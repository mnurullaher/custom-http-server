package com.nurullah.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiFunction;

import static com.nurullah.server.Request.createFromRawRequest;
import static com.nurullah.server.Response.sendResponse;

public class HttpServer {
    private final int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        System.out.println("Server Started");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    var reader = new BufferedReader(
                            new InputStreamReader(client.getInputStream())
                    );
                    var request = createFromRawRequest(reader);
                    String content = handlePath(request);
                    sendResponse(client, "200", "text/plain;charset=UTF-8", content);
                }
            }
        }
    }

//    public String handle(String method, String path, BiFunction<String, String, String> function) {
//
//    }

    public String handlePath(Request request) {
        if ("/ping".equals(request.getPath())) {
            return "pong";
        }
        return "404";
    }
}
