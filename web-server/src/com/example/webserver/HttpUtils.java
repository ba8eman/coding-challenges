package com.example.webserver;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpUtils {
    public static void send404(OutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 13\r\n\r\n" +
                "404 Not Found";
        out.write(response.getBytes());
    }

    public static void send403(OutputStream out) throws IOException {
        String response = "HTTP/1.1 403 Forbidden\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 13\r\n\r\n" +
                "403 Forbidden";
        out.write(response.getBytes());
    }

    public static String detectMimeType(Path path) {
        try {
            String mime = Files.probeContentType(path);
            return mime != null ? mime : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
