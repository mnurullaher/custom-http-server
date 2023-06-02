package com.nurullah.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class RequestUtil {
    public static HttpRequest requestTo(String path) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/" + path))
                .GET()
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}
