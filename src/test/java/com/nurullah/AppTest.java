package com.nurullah;

import com.nurullah.server.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AppTest {
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    public void should_run_server_and_send_request_and_get_response_hello() throws IOException, URISyntaxException, InterruptedException {
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
                .uri(new URI("http://localhost:8080"))
                .GET()
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("hello", response.body());
        thread.interrupt();
    }

}