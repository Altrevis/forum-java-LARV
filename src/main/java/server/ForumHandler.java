package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ForumHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Обработка запроса для пути /forum
        String response = "Hello from the ForumHandler!";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
