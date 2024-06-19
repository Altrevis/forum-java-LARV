package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ForumHandler {

    public static class StaticHandler implements HttpHandler {
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

    public static class SaveUserIDHandler implements HttpHandler {
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

    public static class SaveThreadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] params = requestBody.split("&");
            String title = null;
            String pseudo = null;
            String description = null;

            for (String param : params) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("title")) {
                    title = value;
                } else if (key.equals("pseudo")) {
                    pseudo = value;
                } else if (key.equals("description")) {
                    description = value;
                }
            }

            if (title != null && pseudo != null && description != null) {
                CreateDB.saveThread(title, pseudo, description);

                String response = "Thread saved successfully: " + title;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Title, pseudo or description is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    public static class GetThreadsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = CreateDB.getThreadsFromDB();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static class GetThreadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");
            String threadId = null;

            for (String param : params) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("id")) {
                    threadId = value;
                }
            }

            if (threadId != null) {
                String response = CreateDB.getThreadById(threadId);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Thread ID is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    public static class SaveMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] params = requestBody.split("&");
            String threadID = null;
            String userID = null;
            String message = null;

            for (String param : params) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("threadID")) {
                    threadID = value;
                } else if (key.equals("userID")) {
                    userID = value;
                } else if (key.equals("message")) {
                    message = value;
                }
            }

            if (threadID != null && userID != null && message != null) {
                CreateDB.saveMessage(threadID, userID, message);

                String response = "Message saved successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "ThreadID, userID or message is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}