package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws IOException {
        
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8000), 0);
        
        
        server.createContext("/", new ForumHandler.StaticHandler());
        server.createContext("/save-user-id", new ForumHandler.SaveUserIDHandler());
        server.createContext("/get-threads", new ForumHandler.GetThreadsHandler());
        server.createContext("/get-thread", new ForumHandler.GetThreadHandler());
        server.createContext("/save-thread", new ForumHandler.SaveThreadHandler());
        server.createContext("/save-message", new ForumHandler.SaveMessageHandler());
        server.createContext("/update-like", new ForumHandler.UpdateLikeHandler()); 

        
        server.setExecutor(Executors.newCachedThreadPool());
        
        server.start();

        System.out.println("Server is running on port 8000");
    }
}
