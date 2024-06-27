package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws IOException {
        
        // Create a new HTTP server that listens on all network interfaces on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8000), 0);
        
        // Set up the different contexts and their corresponding handlers
        server.createContext("/", new ForumHandler.StaticHandler());
        server.createContext("/save-user-id", new ForumHandler.SaveUserIDHandler());
        server.createContext("/get-threads", new ForumHandler.GetThreadsHandler());
        server.createContext("/get-thread", new ForumHandler.GetThreadHandler());
        server.createContext("/save-thread", new ForumHandler.SaveThreadHandler());
        server.createContext("/save-message", new ForumHandler.SaveMessageHandler());
        server.createContext("/update-like", new ForumHandler.UpdateLikeHandler());
        server.createContext("/delete-message", new ForumHandler.DeleteMessageHandler());
        server.createContext("/delete-thread", new ForumHandler.DeleteThreadHandler());
        server.createContext("/get-users", new ForumHandler.GetUsersHandler());

        // Set a cached thread pool as the executor for the server
        server.setExecutor(Executors.newCachedThreadPool());
        
        // Start the server
        server.start();

        // Print a message indicating the server is running
        System.out.println("Server is running on port 8000");
    }
}
