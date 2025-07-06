package com.example.webserver;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class WebServer {
    private static final int PORT = 8080;
    private static final Path BASE_DIR = Paths.get("www").toAbsolutePath().normalize();
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(PORT));
        System.out.println("Server listening on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            executor.submit(() -> handleRequest(socket));
        }
    }

    private static void handleRequest(Socket socket) {
        try (
                InputStream iStream = socket.getInputStream();
                OutputStream oStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream))
        ) {
            String line;
            StringBuilder request = new StringBuilder();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                request.append(line).append("\r\n");
            }

            String requestLine = request.toString().split("\r\n")[0];
            if (requestLine.startsWith("GET")) {
                String[] tokens = requestLine.split(" ");
                String urlPath = tokens[1];
                if (urlPath.equals("/")) urlPath = "/html/index.html";
                Path requestedPath = BASE_DIR.resolve(urlPath.substring(1)).normalize();
                if (!requestedPath.startsWith(BASE_DIR)) {
                    HttpUtils.send403(oStream);
                } else if (Files.exists(requestedPath) && !Files.isDirectory(requestedPath)) {
                    byte[] content = Files.readAllBytes(requestedPath);
                    String headers = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + HttpUtils.detectMimeType(requestedPath) + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n\r\n";
                    oStream.write(headers.getBytes());
                    oStream.write(content);
                } else {
                    HttpUtils.send404(oStream);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {}
        }
    }
}
