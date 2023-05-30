package com.nurullah.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
                    var request = createFromRawRequest(new BufferedReader(
                            new InputStreamReader(client.getInputStream())
                    ));
                    sendResponse(client, "200", "text/plain;charset=UTF-8", "hello");
                }
            }
        }
    }
}
