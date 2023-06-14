package com.nurullah.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.nurullah.server.Request.createFromRawRequest;

@Slf4j
public class HttpServer {

    private static final ExecutorService executor = Executors.newFixedThreadPool(100);
    private static final Map<String, RequestHandler> pathHandlers = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(new ConnectionAcceptor(serverSocket, port)).start();
    }

    public void shutDown() throws IOException {
        serverSocket.close();
    }

    public void handle(String method, String path, RequestHandler function) {
        pathHandlers.put("%s-%s".formatted(method, path), function);
    }

    private record ConnectionAcceptor(ServerSocket serverSocket, int port) implements Runnable {
        @Override
        public void run() {
            log.info("Server Started at PORT: " + port);
            while (!Thread.interrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    log.info("New client excepted, addr: {}", client.getRemoteSocketAddress());
                    executor.execute(new HandleClient(client));
                } catch (IOException e) {
                    if (!serverSocket.isClosed())
                        throw new RuntimeException(e);
                }
            }
        }
    }

    private record HandleClient(Socket client) implements Runnable {

        private static final RequestHandler NotFoundHandler = (req, resp) -> {
          resp.setStatus("404");
          resp.setContent("");
        };

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Request request;
                    request = createFromRawRequest(new BufferedReader(
                                    new InputStreamReader(client.getInputStream())
                            )
                    );
                    log.info("New request to: " + request.getPath());
                    var response = new Response();
                    var function = Objects.requireNonNullElse(
                            pathHandlers.get("%s-%s".formatted(request.getMethod(), request.getPath())),
                            NotFoundHandler
                    );
                    function.apply(request, response);
                    var clientOutput = client.getOutputStream();
                    var connectionHeader = Objects.requireNonNullElse(
                            request.getHeaders().get("Connection"), "keep-alive"
                    );
                    clientOutput.write(getRawResponse(response, connectionHeader).getBytes());
                    clientOutput.flush();
                    if (connectionHeader.equals("Close")) {
                        client.close();
                        break;
                    }
                } catch (IOException e) {
                    log.error("Client handling failed, ERR is: {}", e.getLocalizedMessage());
                    break;
                } catch (InvalidRequestException e) {
                    log.error("Invalid request!");
                    try {
                        client.close();
                    } catch (IOException ex) {
                        throw new RuntimeException();
                    }
                    break;
                }
            }
        }
    }

    private static String getRawResponse(Response response, String connectionHeader) throws IOException {
        String serializedContent = response.getContent() == null ? "" :
                response.getContent() instanceof String ? (String) response.getContent() :
                        objectMapper.writeValueAsString(response.getContent());

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ")
                .append(response.getStatus()).append("\r\n")
                .append("ContentType: ")
                .append(response.getContentType()).append("\r\n")
                .append("Connection: ").append(connectionHeader).append("\r\n")
                .append("Content-Length: ")
                .append(serializedContent.length());
        response.getHeaders().forEach((key, val) -> responseBuilder.append("\r\n")
                .append(key).append(": ")
                .append(val));
        responseBuilder
                .append("\r\n\r\n")
                .append(serializedContent);
        log.info("RESPONSE:\n{}", responseBuilder);
        return responseBuilder.toString();
    }
}
