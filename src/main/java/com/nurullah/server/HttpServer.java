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

import static com.nurullah.server.Request.createFromRawRequest;

public class HttpServer {

    private static final Map<String, RequestHandler> pathHandlers = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private ServerSocket serverSocket;
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(new ConnectionAcceptor(serverSocket, port)).start();
    }

    public void shutDown() throws IOException {
        serverSocket.close();
    }

    private record ConnectionAcceptor(ServerSocket serverSocket, int port) implements Runnable {
        @Override
        public void run() {

            logger.info("Server Started at PORT: " + port);
            while (!Thread.interrupted()) {
                try (Socket client = serverSocket.accept()) {
                    var request = createFromRawRequest(new BufferedReader(
                                    new InputStreamReader(client.getInputStream())
                            )
                    );
                    var response = new Response();
                    logger.info("New request to: " + request.getPath());
                    var function = pathHandlers.get("%s-%s".formatted(request.getMethod(), request.getPath()));
                    handleRequest(function, request, response);
                    sendResponse(response, client);
                } catch (IOException e) {
                    if (!serverSocket.isClosed())
                        throw new RuntimeException(e);
                }
            }
        }
    }

    public void handle(String method, String path, RequestHandler function) {
        pathHandlers.put("%s-%s".formatted(method, path), function);
    }

    private static void handleRequest(RequestHandler function, Request request, Response response) throws JsonProcessingException {
        if (function != null) {
            function.apply(request, response);
        } else {
            response.setStatus("404");
            response.setContent("");
        }
    }

    private static void sendResponse(Response response, Socket client) throws IOException {
        String serializedContent = response.getContent() == null ? "" :
                response.getContent() instanceof String ? (String) response.getContent() :
                        objectMapper.writeValueAsString(response.getContent());

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ")
                .append(response.getStatus()).append("\r\n")
                .append("ContentType: ")
                .append(response.getContentType()).append("\r\n")
                .append("Content-Length: ")
                .append(serializedContent.length());
        response.getHeaders().forEach((key, val) -> responseBuilder.append("\r\n")
                .append(key).append(": ")
                .append(val));
        responseBuilder
                .append("\r\n\r\n")
                .append(serializedContent);

        logger.info("RESPONSE:\n" + responseBuilder);
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(responseBuilder.toString().getBytes());
        clientOutput.flush();
        client.close();
    }
}
