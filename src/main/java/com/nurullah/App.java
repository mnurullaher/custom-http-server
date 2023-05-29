package com.nurullah;

import com.nurullah.server.HttpServer;

import java.io.IOException;

public class App
{
    public static void main( String[] args ) throws IOException {
        HttpServer server = new HttpServer(8080);
        server.startServer();
    }
}
