package com.nurullah.server;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Request {
    private String method;
    private String path;
    private String version;
    private String host;
    private List<String> headers;

    public static Request createFromRawRequest(BufferedReader reader) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null && line.length() > 0) {
                requestBuilder.append(line).append("\r\n");
        }

        var request = new Request();

        String[] requestsLines = requestBuilder.toString().split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        if (requestLine.length != 3) {
            System.out.println("Bozuk req: " + requestBuilder.toString());
        }
        request.method = requestLine[0];
        request.path = requestLine[1];
        request.version = requestLine[2];
        request.host = requestsLines[1].split(" ")[1];
        request.headers = new ArrayList<>(Arrays.asList(requestsLines).subList(2, requestsLines.length));

        return request;
    }


}
