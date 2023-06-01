package com.nurullah.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Response {
    private String status = "200";
    private String contentType = "text/plain;charset=UTF-8";
    @Setter(AccessLevel.PRIVATE)
    private String content = "";
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.NONE)
    private Map<String, String> headers = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public <T> void handleContent(T content, Response response) throws JsonProcessingException {
        if (!content.getClass().getSimpleName().equals("String")) {
            response.setContent(objectMapper.writeValueAsString(content));
        } else {
            response.setContent((String) content);
        }
    }
}
