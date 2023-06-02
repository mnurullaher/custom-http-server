package com.nurullah;

import com.nurullah.server.HttpServer;
import com.nurullah.util.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

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
    @MethodSource
    public void should_handle_http_requests(
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

    private static Stream<Arguments> should_handle_http_requests() throws URISyntaxException {
        return Stream.of(
                Arguments.of(requestTo("test"), 200, "New Header", "{\"name\":\"Nurullah\",\"age\":25}"),
                Arguments.of(requestTo("notfound"), 404, "", "")
        );
    }

    private static HttpRequest requestTo(String path) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/" + path))
                .GET()
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}