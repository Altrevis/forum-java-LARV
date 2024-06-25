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

    /**
     * Handles static file requests (HTML, CSS, JS).
     */
    public static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestURI = exchange.getRequestURI().getPath();

            // Default to index.html if root path ("/") is requested
            if (requestURI.equals("/")) {
                requestURI = "/index.html";
            }

            // Determine MIME type based on file extension
            String mimeType = getMimeType(requestURI);

            // Retrieve the requested file
            File file = new File("src/web" + requestURI);
            if (file.exists() && file.isFile()) {
                // Send the file as a response if it exists
                sendFileResponse(file, exchange, mimeType);
            } else {
                // Return 404 Not Found if file does not exist
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        /**
         * Determines the MIME type of a requested resource based on its file extension.
         *
         * @param requestURI The URI of the requested resource.
         * @return The MIME type corresponding to the resource.
         */
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

        /**
         * Sends the requested file as a response.
         *
         * @param file     The file to be sent.
         * @param exchange The HttpExchange object representing the HTTP request and response.
         * @param mimeType The MIME type of the file.
         * @throws IOException If an I/O error occurs.
         */
        private void sendFileResponse(File file, HttpExchange exchange, String mimeType) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            Files.copy(file.toPath(), os);
            os.close();
        }
    }

    /**
     * Handles saving a user ID received via a POST request.
     */
    public static class SaveUserIDHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }

            // Extract user ID from request body
            String requestBody = convertStreamToString(exchange.getRequestBody());
            String[] params = requestBody.split("&");
            String userID = null;
            for (String param : params) {
                if (param.startsWith("userID=")) {
                    userID = param.substring("userID=".length());
                    break;
                }
            }

            // Save user ID to database if present; otherwise, return error
            if (userID != null) {
                CreateDB.saveUserID(userID);
                String response = "UserID saved successfully: " + userID;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "UserID is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length); // Bad Request
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        /**
         * Converts an input stream to a string.
         *
         * @param inputStream The input stream to convert.
         * @return The string representation of the input stream.
         */
        private String convertStreamToString(java.io.InputStream inputStream) {
            StringBuilder sb = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                if (scanner.hasNext()) {
                    sb.append(scanner.next());
                }
            }
            return sb.toString();
        }
    }

    /**
     * Handles saving a new forum thread received via a POST request.
     */
    public static class SaveThreadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }

            // Extract title, pseudo (author), and description from request body
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

            // Save thread to database if all required parameters are present; otherwise, return error
            if (title != null && pseudo != null && description != null) {
                CreateDB.saveThread(title, pseudo, description);

                String response = "Thread saved successfully: " + title;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Title, pseudo or description is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length); // Bad Request
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    /**
     * Handles retrieving all forum threads.
     */
    public static class GetThreadsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Retrieve all threads from database and send as response
            String response = CreateDB.getThreadsFromDB();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * Handles retrieving a specific forum thread by ID.
     */
    public static class GetThreadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Extract thread ID from query parameters
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

            // Retrieve thread by ID from database and send as response if ID is provided; otherwise, return error
            if (threadId != null) {
                String response = CreateDB.getThreadById(threadId);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Thread ID is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length); // Bad Request
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    /**
     * Handles saving a new message in a forum thread.
     */
    public static class SaveMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }
    
            // Extract thread ID, user ID, and message content from request body
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
    
            // Save message to database if all required parameters are present; otherwise, return error
            if (threadID != null && userID != null && message != null) {
                int messageId = CreateDB.saveMessage(threadID, userID, message);
    
                String response = String.valueOf(messageId);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "ThreadID, userID or message is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length); // Bad Request
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    

    /**
     * Handles liking a message in a forum thread.
     */
    public static class LikeMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }
    
            // Extract message ID from query parameters
            String query = exchange.getRequestURI().getQuery();
            String messageId = getQueryParam(query, "id");
    
            // Like the message and return the number of likes if successful; otherwise, return appropriate error
            if (messageId != null) {
                int likes = CreateDB.likeMessage(messageId);
                if (likes != -1) {
                    String response = String.valueOf(likes);
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(500, 0); // Internal Server Error
                }
            } else {
                exchange.sendResponseHeaders(400, 0); // Bad Request
            }
        }
    }
    
    /**
     * Handles disliking a message in a forum thread.
     */
    public static class DislikeMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }
    
            // Extract message ID from query parameters
            String query = exchange.getRequestURI().getQuery();
            String messageId = getQueryParam(query, "id");
    
            // Dislike the message and return the number of dislikes if successful; otherwise, return appropriate error
            if (messageId != null) {
                int dislikes = CreateDB.dislikeMessage(messageId);
                if (dislikes != -1) {
                    String response = String.valueOf(dislikes);
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(500, 0); // Internal Server Error
                }
            } else {
                exchange.sendResponseHeaders(400, 0); // Bad Request
            }
        }
    }
    
    /**
     * Extracts a query parameter from a query string.
     *
     * @param query The query string containing parameters.
     * @param param The parameter key to retrieve.
     * @return The value of the parameter if found; otherwise, null.
     */
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
 
    /**
     * Handles updating a like or dislike on a message.
     */
    public static class UpdateLikeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }
    
            // Extract message ID, like/dislike status, and user ID from request body
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
    
            // Update like/dislike status for the message if both message ID and user ID are provided; otherwise, return error
            if (messageId != null && userID != null) {
                CreateDB.handleReaction(userID, messageId, isLike);
    
                String response = "Like/Dislike updated successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Message ID or User ID is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length); // Bad Request
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    

    /**
     * Handles deleting a message from a forum thread.
     */
    public static class DeleteMessageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, 0);
            return;
        }

        // Extract message ID from query parameters
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String[] params = requestBody.split("&");
        String messageID = null;

        for (String param : params) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            if (key.equals("messageID")) {
                messageID = value;
            }
        }

         // Delete the message from the database if message ID is provided; otherwise, return error
        if (messageID != null) {
            boolean deleted = CreateDB.deleteMessageById(messageID);
            if (deleted) {
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(404, 0);
            }
        } else {
            exchange.sendResponseHeaders(400, 0); // Bad Request
        }

        exchange.getResponseBody().close();
    }
}


    /**
     * Handles deleting a thread from the forum.
     */
    public static class DeleteThreadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                return;
            }
    
            // Extract thread ID from query parameters
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
    
            // Delete the thread from the database if thread ID is provided; otherwise, return error
            if (threadId != null) {
                CreateDB.deleteThread(threadId);
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(400, 0); // Bad Request
            }
    
            exchange.getResponseBody().close();
        }
    }
}
