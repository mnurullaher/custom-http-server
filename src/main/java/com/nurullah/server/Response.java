package com.nurullah.server;

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
    private Object content = "";
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.NONE)
    private Map<String, String> headers = new HashMap<>();

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }
}
