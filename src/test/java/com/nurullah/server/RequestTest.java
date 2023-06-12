package com.nurullah.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;

import static com.nurullah.server.Request.createFromRawRequest;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTest {

    @Mock
    private BufferedReader bufferedReader;
    @Test
    void should_create_request_object_with_read_line_input() throws IOException, InvalidRequestException {
        when(bufferedReader.readLine())
                .thenReturn("GET /test HTTP/1.1")
                .thenReturn("Content-Length: 0")
                .thenReturn("Host: localhost:8080")
                .thenReturn("User-Agent: User-Agent")
                .thenReturn("Content-Type: text/plain")
                .thenReturn("");

        var result = createFromRawRequest(bufferedReader);

        then(result).isNotNull();
        then(result.getMethod()).isEqualTo("GET");
        then(result.getPath()).isEqualTo("/test");
        then(result.getVersion()).isEqualTo("HTTP/1.1");
        then(result.getHost()).isEqualTo("localhost:8080");
        then(result.getHeaders().size()).isEqualTo(4);
    }
}