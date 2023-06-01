package com.nurullah.server;

import com.fasterxml.jackson.core.JsonProcessingException;

@FunctionalInterface
public interface RequestHandler {
    void apply(Request req, Response res) throws JsonProcessingException;
}
