package com.nurullah.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.nurullah.server.Request.createFromRawRequest;

public class HttpServer {

    private static final ExecutorService executor = Executors.newFixedThreadPool(100);
    private static final Map<String, RequestHandler> pathHandlers = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
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
            logger.info("Server Started at PORT: " + port);
            while (!Thread.interrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    executor.execute(new HandleClient(client));
                } catch (IOException e) {
                    if (!serverSocket.isClosed())
                        throw new RuntimeException(e);
                }
            }
        }
    }

    private record HandleClient(Socket client) implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Request request;
                    try {
                        request = createFromRawRequest(new BufferedReader(
                                        new InputStreamReader(client.getInputStream())
                                )
                        );
                    } catch (InvalidRequestException e) {
                        logger.error("Invalid request!");
                        client.close();
                        return;
                    }
                    logger.info("New request to: " + request.getPath());
                    var response = new Response();
                    var function = pathHandlers.get("%s-%s".formatted(request.getMethod(), request.getPath()));
                    handleRequest(function, request, response);
                    if (sendResponseAndCloseConnection(client, request, response)) break;
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }
    }

    private static void handleRequest(RequestHandler function, Request request, Response response) throws JsonProcessingException {
        if (function != null) {
            function.apply(request, response);
        } else {
            response.setStatus("404");
            response.setContent("");
        }
    }

    private static boolean sendResponseAndCloseConnection(Socket client, Request request, Response response) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        String connectionHeader = request.getHeaders().get("Connection: ");
        if (connectionHeader == null || !connectionHeader.equals("keep-alive")) {
            clientOutput.write(getRawResponse(response, "Close").getBytes());
            clientOutput.flush();
            client.close();
            return true;
        } else {
            clientOutput.write(getRawResponse(response, "keep-alive").getBytes());
            clientOutput.flush();
        }
        return false;
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
        return responseBuilder.toString();
    }
}
