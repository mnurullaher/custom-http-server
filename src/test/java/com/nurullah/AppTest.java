package com.nurullah;

import com.nurullah.server.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AppTest {
    @Test
    public void should_send_request_and_get_response_according_to_handle() throws IOException, URISyntaxException, InterruptedException {

        var server = new HttpServer(8080);
        server.handle("GET", "/ping", (request, response) -> {
            response.setStatus("200");
            response.setContent("customContent");
        });
        var thread = new Thread(() -> {
            try {
                server.startServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/ping"))
                .GET()
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("customContent", response.body());
        thread.interrupt();
    }

    @Test
    public void should_return_status_code_404_for_requests_to_non_registered_paths() throws URISyntaxException, IOException, InterruptedException {
        var server = new HttpServer(8080);
        var thread = new Thread(() -> {
            try {
                server.startServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/users"))
                .GET()
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        thread.interrupt();
    }

    @Test
    public void should_be_able_to_add_response_headers() throws IOException, URISyntaxException, InterruptedException {

        var server = new HttpServer(8080);
        server.handle("GET", "/header", (request, response) -> {
            response.getHeaders().put("First-Header", "this is first header");
            response.getHeaders().put("Second-Header", "this is second header");
        });
        var thread = new Thread(() -> {
            try {
                server.startServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/header"))
                .GET()
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("this is first header", response.headers().firstValue("First-Header").orElse(""));
        assertEquals("this is second header", response.headers().firstValue("Second-Header").orElse(""));
        thread.interrupt();
    }

}