package com.forum;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();
    private static final String WEB_ROOT = "forum_web/";

    public static void main(String[] args) {
        int port = 12345;
        try {
            String serverAddress = "127.0.0.1";
            try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(serverAddress))) {
                System.out.println("Server started on port " + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());

                    // Determine if the connection is for HTTP handling or client handling
                    new Thread(() -> handleConnection(clientSocket)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleConnection(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String requestLine = in.readLine();
            if (requestLine != null && requestLine.startsWith("GET")) {
                // Handle HTTP request
                new HttpHandler(clientSocket, requestLine).run();
            } else {
                // Handle client connection
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void clientDisconnected(ClientHandler client) {
        System.out.println("Client disconnected: " + client.getClientSocket().getInetAddress());
        clients.remove(client);
    }

    static class HttpHandler implements Runnable {
        private Socket socket;
        private String requestLine;

        public HttpHandler(Socket socket, String requestLine) {
            this.socket = socket;
            this.requestLine = requestLine;
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream());
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String[] tokens = requestLine.split(" ");
                if (tokens.length == 3 && tokens[0].equals("GET")) {
                    String filePath = tokens[1].equals("/") ? "index.html" : tokens[1].substring(1);
                    filePath = WEB_ROOT + filePath;
                    System.out.println("Serving file: " + filePath);

                    if (Files.exists(Paths.get(filePath))) {
                        byte[] content = Files.readAllBytes(Paths.get(filePath));
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: " + getContentType(filePath));
                        out.println("Content-Length: " + content.length);
                        out.println();
                        out.flush();
                        socket.getOutputStream().write(content);
                        socket.getOutputStream().flush();
                    } else {
                        System.out.println("File not found: " + filePath);
                        out.println("HTTP/1.1 404 Not Found");
                        out.println();
                        out.flush();
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getContentType(String filePath) {
            if (filePath.endsWith(".html")) {
                return "text/html";
            } else if (filePath.endsWith(".css")) {
                return "text/css";
            } else if (filePath.endsWith(".js")) {
                return "application/javascript";
            } else {
                return "application/octet-stream";
            }
        }
    }
}
