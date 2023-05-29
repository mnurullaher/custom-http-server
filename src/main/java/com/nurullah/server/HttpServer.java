package com.nurullah.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.nurullah.server.Request.getRawRequest;

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
                    var request = new Request(getRawRequest(client));
                    System.out.println(request.getMethod());
                    System.out.println(request.getPath());
                    System.out.println(request.getVersion());
                    System.out.println(request.getHeaders());
                }
            }
        }
    }
}
