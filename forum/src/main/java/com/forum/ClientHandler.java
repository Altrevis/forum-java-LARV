package com.forum;

import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter writer;

    // Constructeur prenant un socket client en argument
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    // Méthode exécutée par le thread lorsqu'il démarre
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            // Boucle pour lire les messages du client en continu
            while ((message = reader.readLine()) != null) {
                System.out.println("Message du client : " + message);
                if (message.equals("request_file")) {
                } else {
                    Server.broadcastMessage("Client " + clientSocket.getInetAddress() + " : " + message, this); // Diffuser le message à tous les clients
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close(); // Fermer le socket client
                Server.clientDisconnected(this); // Gérer la déconnexion du client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour envoyer un message au client
    public void sendMessage(String message) {
        writer.println(message);
    }

    // Méthode pour envoyer un fichier au client
    public void sendFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            OutputStream outputStream = clientSocket.getOutputStream();
            // Lire et écrire le fichier en morceaux
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            fileInputStream.close(); // Fermer le flux d'entrée du fichier
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour obtenir le socket client
    public Socket getClientSocket() {
        return clientSocket;
    }
}

