package com.nurullah.server;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@Getter
public class Request {
    private String method;
    private String path;
    private String version;
    private String body;
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
        var isMethodValid = Arrays.stream(RequestMethod.values())
                .map(Enum::name)
                .anyMatch(v -> v.equals(requestLine[0]));
        var isPathValid = requestLine[1].charAt(0) == '/';
        var isVersionValid = List.of("HTTP/1.1", "HTTP/1.0").contains(requestLine[2]);
        if (requestLine.length != 3 || !isMethodValid || !isPathValid || !isVersionValid) {
            throw new InvalidRequestException();
        }
        request.method = requestLine[0];
        request.path = requestLine[1];
        request.version = requestLine[2];
        Arrays.asList(requestsLines).subList(1, requestsLines.length).forEach(l -> {
            var headerLine = l.split(" ");
            request.headers.put(headerLine[0].replace(":", ""), headerLine[1]);
        });

        var contentLength = Integer.parseInt(
                Objects.requireNonNullElse(
                        request.getHeaders().get("Content-Length"),
                        "0"
                )
        );
        var content = new char[contentLength];
        var readLength = reader.read(content);
        if (contentLength != readLength)
            throw new InvalidRequestException();
        request.body = new String(content);

        return request;
    }


}
