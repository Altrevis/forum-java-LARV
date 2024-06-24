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
                int messageId = CreateDB.saveMessage(threadID, userID, message);
    
                String response = String.valueOf(messageId);
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
    

    public static class LikeMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }
    
            String query = exchange.getRequestURI().getQuery();
            String messageId = getQueryParam(query, "id");
    
            if (messageId != null) {
                int likes = CreateDB.likeMessage(messageId);
                if (likes != -1) {
                    String response = String.valueOf(likes);
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(500, 0);
                }
            } else {
                exchange.sendResponseHeaders(400, 0);
            }
        }
    }
    
    public static class DislikeMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }
    
            String query = exchange.getRequestURI().getQuery();
            String messageId = getQueryParam(query, "id");
    
            if (messageId != null) {
                int dislikes = CreateDB.dislikeMessage(messageId);
                if (dislikes != -1) {
                    String response = String.valueOf(dislikes);
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(500, 0);
                }
            } else {
                exchange.sendResponseHeaders(400, 0);
            }
        }
    }
    
    private static String getQueryParam(String query, String param) {
        String[] params = query.split("&");
        for (String p : params) {
            String[] pair = p.split("=");
            if (pair.length > 1 && pair[0].equals(param)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
 
    public static class UpdateLikeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }
    
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] params = requestBody.split("&");
            String messageId = null;
            boolean isLike = false;
            String userID = null;
    
            for (String param : params) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("messageId")) {
                    messageId = value;
                } else if (key.equals("isLike")) {
                    isLike = Boolean.parseBoolean(value);
                } else if (key.equals("userID")) {
                    userID = value;
                }
            }
    
            if (messageId != null && userID != null) {
                CreateDB.handleReaction(userID, messageId, isLike);
    
                String response = "Like/Dislike updated successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Message ID or User ID is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    

    public static class DeleteMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }
    
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");
            String messageId = null;
    
            for (String param : params) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("id")) {
                    messageId = value;
                    break;
                }
            }
    
            if (messageId != null) {
                CreateDB.deleteMessage(messageId);
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(400, 0);
            }
    
            exchange.getResponseBody().close();
        }
    }

    public static class DeleteThreadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }
    
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");
            String threadId = null;
    
            for (String param : params) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                if (key.equals("id")) {
                    threadId = value;
                    break;
                }
            }
    
            if (threadId != null) {
                CreateDB.deleteThread(threadId);
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(400, 0);
            }
    
            exchange.getResponseBody().close();
        }
    }
    

}
