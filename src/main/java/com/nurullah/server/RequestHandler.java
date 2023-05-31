package com.nurullah.server;

public interface RequestHandler {
    void apply(Request req, Response res);
}
