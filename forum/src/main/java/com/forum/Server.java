package com.forum;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        int port = 12345;

        try {
            String serverAddress = "127.0.0.1"; 
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(serverAddress));
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                SwingUtilities.invokeLater(() -> new LoginPage(new HashMap<>(), clientSocket));

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
}