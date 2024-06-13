package com.forum;

import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
             
            this.writer = writer;
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Message from client: " + message);
                if (!message.equals("request_file")) {
                    Server.broadcastMessage("Client " + clientSocket.getInetAddress() + ": " + message, this);
                }
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                e.printStackTrace();
            }
        } finally {
            try {
                clientSocket.close();
                Server.clientDisconnected(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
