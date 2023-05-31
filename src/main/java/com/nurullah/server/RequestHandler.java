package com.nurullah.server;

@FunctionalInterface
public interface RequestHandler {
    void apply(Request req, Response res);
}
