package com.nurullah.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Response {

    public static void sendResponse(Socket client, String status, String contentType, String content) throws IOException {

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ")
                .append(status)
                .append(System.getProperty("line.separator"))
                .append("ContentType: ")
                .append(contentType)
                .append(System.getProperty("line.separator"))
                .append("Content-Length: ")
                .append(content.length())
                .append(System.getProperty("line.separator"))
                .append(System.getProperty("line.separator"))
                .append(content);
        System.out.println(responseBuilder.toString());
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(responseBuilder.toString().getBytes());
        clientOutput.flush();
        client.close();
    }

}
