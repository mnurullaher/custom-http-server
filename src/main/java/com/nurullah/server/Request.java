package com.nurullah.server;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Request {
    private String method;
    private String path;
    private String version;
    private String host;
    private List<String> headers;

    public Request(String rawRequest) {
        String[] requestsLines = rawRequest.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        this.method = requestLine[0];
        this.path = requestLine[1];
        this.version = requestLine[2];
        this.host = requestsLines[1].split(" ")[1];
        this.headers = new ArrayList<>(Arrays.asList(requestsLines).subList(2, requestsLines.length));
    }
    public static String getRawRequest(Socket client) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringBuilder requestBuilder = new StringBuilder();
        String line;

        while (!(line = reader.readLine()).isBlank()) {
            requestBuilder.append(line).append("\r\n");
        }

        return requestBuilder.toString();
    }



}
