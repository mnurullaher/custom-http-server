package com.nurullah.server;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Request {
    private String method;
    private String path;
    private String version;
    private final Map<String, String> headers = new HashMap<>();

    public String getHost() {
        return headers.get("Host");
    }

    public static Request createFromRawRequest(BufferedReader reader) throws IOException, InvalidRequestException {
        StringBuilder requestBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null && line.length() > 0) {
            requestBuilder.append(line).append("\r\n");
        }

        var request = new Request();

        String[] requestsLines = requestBuilder.toString().split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        if (requestLine.length != 3) {
            throw new InvalidRequestException();
        }
        request.method = requestLine[0];
        request.path = requestLine[1];
        request.version = requestLine[2];
        Arrays.asList(requestsLines).subList(1, requestsLines.length).forEach(l -> {
            var headerLine = l.split(" ");
            request.headers.put(headerLine[0].replace(":", ""), headerLine[1]);
        });
        return request;
    }


}
