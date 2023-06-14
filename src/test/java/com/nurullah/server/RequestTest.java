package com.nurullah.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;

import static com.nurullah.server.Request.createFromRawRequest;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestTest {

    @Test
    public void should_create_request_object_with_read_line_input() throws IOException, InvalidRequestException {
        var rawRequest = """
                GET /test HTTP/1.1
                Content-Length: 7
                Host: localhost:8080
                User-Agent: java
                Content-Type: test/plain
                                
                content
                """;
        var inputString = new StringReader(rawRequest);
        var reader = new BufferedReader(inputString);

        var result = createFromRawRequest(reader);

        then(result).isNotNull();
        then(result.getMethod()).isEqualTo("GET");
        then(result.getPath()).isEqualTo("/test");
        then(result.getVersion()).isEqualTo("HTTP/1.1");
        then(result.getHost()).isEqualTo("localhost:8080");
        then(result.getHeaders().size()).isEqualTo(4);
        then(result.getBody()).isEqualTo("content");
    }


    @ParameterizedTest
    @MethodSource
    public void should_throw_invalid_request_exception_in_bad_requests(
            String method,
            String path,
            String version,
            String content
    ) {
        var rawRequest = """
                %s %s %s
                Content-Length: 7
                Host: localhost:8080
                User-Agent: java
                Content-Type: test/plain
                                
                %s
                """;
        var formattedReq = String.format(rawRequest, method, path, version, content);
        var inputString = new StringReader(formattedReq);
        var reader = new BufferedReader(inputString);

        assertThrows(InvalidRequestException.class, () -> createFromRawRequest(reader));
    }

    private static Stream<Arguments> should_throw_invalid_request_exception_in_bad_requests() {
        return Stream.of(
                Arguments.of("XYZ", "/test", "HTTP/1.1", "content"),
                Arguments.of("GET", "/test", "HTTP/1.0", "cont"),
                Arguments.of("GET", "/test", "HTTP/1.1", ""),
                Arguments.of("", "/test", "HTTP/1.1", "content"),
                Arguments.of("", "/test", "HTTP/1.1", ""),
                Arguments.of("GET", "/test", "HTTP/**", "content"),
                Arguments.of("GET", "xyz", "HTTP/1.1", "content")
        );
    }
}