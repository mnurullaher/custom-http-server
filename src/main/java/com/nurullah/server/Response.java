package com.nurullah.server;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Response {
    private String status = "200";
    private String contentType = "text/plain;charset=UTF-8";
    private String content = "";
    private Map<String, String> headers = new HashMap<>();
}
