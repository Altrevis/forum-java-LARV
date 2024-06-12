package com.forum;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int port = 12345;

        try {
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Connected to the server");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new BufferedReader(new InputStreamReader(System.in));

            out.println("Hello from client");

            String response = in.readLine();
            System.out.println("Server response: " + response);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}