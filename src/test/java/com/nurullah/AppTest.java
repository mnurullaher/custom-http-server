package com.nurullah;

import com.nurullah.server.HttpServer;
import com.nurullah.server.Model;
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
    public void should_send_request_and_get_response_with_handle_method() throws IOException, URISyntaxException, InterruptedException {

        var server = new HttpServer(8080);
        server.handle("GET", "/ping", (request, response) -> {
            response.setStatus("200");
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
            response.addHeader("First-Header", "this is first header");
            response.removeHeader("Second-Header");
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
        assertEquals("", response.headers().firstValue("Second-Header").orElse(""));
        thread.interrupt();
    }

    @Test
    public void should_handle_non_primitive_response_content() throws IOException, URISyntaxException, InterruptedException {

        var server = new HttpServer(8080);
        server.handle("GET", "/header", (request, response) -> {
            response.handleContent(new Model("nurullah", 25), response);
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

        assertEquals("{\"name\":\"nurullah\",\"age\":25}", response.body());
        thread.interrupt();
    }

}