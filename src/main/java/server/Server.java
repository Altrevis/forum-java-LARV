package server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new StaticHandler());
        server.createContext("/save-user-id", new SaveUserIDHandler());
        server.createContext("/get-threads", new GetThreadsHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Server is running on port 8000");
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestURI = exchange.getRequestURI().getPath();

            if (requestURI.equals("/")) {
                requestURI = "/index.html";
            }

            String mimeType = getMimeType(requestURI);

            File file = new File("src/web" + requestURI);
            if (file.exists() && file.isFile()) {
                sendFileResponse(file, exchange, mimeType);
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String getMimeType(String requestURI) {
            if (requestURI.endsWith(".html")) {
                return "text/html";
            } else if (requestURI.endsWith(".css")) {
                return "text/css";
            } else if (requestURI.endsWith(".js")) {
                return "application/javascript";
            } else {
                return "text/plain";
            }
        }

        private void sendFileResponse(File file, HttpExchange exchange, String mimeType) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            Files.copy(file.toPath(), os);
            os.close();
        }
    }

    static class SaveUserIDHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); 
                return;
            }
    
            String requestBody = Utils.convertStreamToString(exchange.getRequestBody());
            String[] params = requestBody.split("&");
            String userID = null;
            for (String param : params) {
                if (param.startsWith("userID=")) {
                    userID = param.substring("userID=".length());
                    break;
                }
            }
    
            if (userID != null) {
                CreateDB.saveUserID(userID);
                String response = "UserID saved successfully: " + userID;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "UserID is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    
    static class GetThreadsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = CreateDB.getThreadsFromDB();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    
        
    }
}