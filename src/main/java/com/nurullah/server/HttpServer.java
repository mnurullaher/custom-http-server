package com.nurullah.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.nurullah.server.Request.createFromRawRequest;

public class HttpServer {
    private final int port;
    private final Map<String, BiFunction<Request, Response, Void>> pathHandlers = new HashMap<>();

    public HttpServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        System.out.println("Server Started");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    var request = createFromRawRequest(new BufferedReader(
                                    new InputStreamReader(client.getInputStream())
                            )
                    );
                    var response = new Response(client, "text/plain;charset=UTF-8");
                    System.out.println("New request to: " + request.getPath());
                    var function = pathHandlers.get("%s-%s".formatted(request.getMethod(), request.getPath()));
                    function.apply(request, response);
                    sendResponse(response);
                }
            }
        }
    }

    public void handle(String method, String path, BiFunction<Request, Response, Void> function) {
        pathHandlers.put("%s-%s".formatted(method, path), function);
    }

    public void sendResponse(Response response) throws IOException {

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ")
                .append(response.getStatus())
                .append(System.getProperty("line.separator"))
                .append("ContentType: ")
                .append(response.getContentType())
                .append(System.getProperty("line.separator"))
                .append("Content-Length: ")
                .append(response.getContent().length())
                .append(System.getProperty("line.separator"))
                .append(System.getProperty("line.separator"))
                .append(response.getContent());
        System.out.println(responseBuilder);
        OutputStream clientOutput = response.getClient().getOutputStream();
        clientOutput.write(responseBuilder.toString().getBytes());
        clientOutput.flush();
        response.getClient().close();
    }
}
