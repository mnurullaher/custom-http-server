package com.nurullah;

import com.nurullah.server.HttpServer;
import com.nurullah.util.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    HttpServer server;

    @BeforeEach
    public void before() throws IOException {
        server = new HttpServer();

        server.handle("GET", "/test", (request, response) -> {
            response.setContent(new Model("Nurullah", 25));
            response.addHeader("New-Header", "New Header");
            response.addHeader("Deleted-Header", "Deleted");
            response.removeHeader("Deleted-Header");
        });

        server.start(8080);
    }

    @AfterEach
    public void after() throws IOException {
        server.shutDown();
    }

    @ParameterizedTest
    @MethodSource("com.nurullah.util.Parameter#getParameters")
    public void test(
            HttpRequest httpRequest,
            int expectedStatus,
            String expectedHeader,
            String expectedContent) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(expectedStatus, response.statusCode());
        assertEquals(expectedHeader, response.headers().firstValue("New-Header").orElse(""));
        assertEquals("", response.headers().firstValue("Deleted-Header").orElse(""));
        assertEquals(expectedContent, response.body());
    }
}