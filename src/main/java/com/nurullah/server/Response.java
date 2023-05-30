package com.nurullah.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.Socket;

@Getter
@Setter
@RequiredArgsConstructor
public class Response {

    private final Socket client;
    private String status;
    private final String contentType;
    private String content;

}
