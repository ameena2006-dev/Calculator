import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CalculatorServer {

    public static void main(String[] args) throws IOException {

        // Default port
        int port = 8080;

        // Allow custom port:
        // java CalculatorServer 8081
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number.");
                return;
            }
        }

        HttpServer server = HttpServer.create(
                new InetSocketAddress(port), 0
        );

        // Home page
        server.createContext("/", CalculatorServer::home);

        // Calculator API
        server.createContext(
                "/calculate",
                CalculatorServer::calculate
        );

        server.start();

        System.out.println("================================");
        System.out.println("Calculator Server Started!");
        System.out.println("Open: http://localhost:" + port);
        System.out.println("================================");
    }


    // =========================
    // HOME PAGE
    // =========================

    private static void home(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        // Serve index.html
        if (path.equals("/") || path.equals("/index.html")) {

            sendFile(
                    exchange,
                    "index.html",
                    "text/html; charset=UTF-8"
            );

            return;
        }

        // Serve CSS
        if (path.equals("/style.css")) {

            sendFile(
                    exchange,
                    "style.css",
                    "text/css; charset=UTF-8"
            );

            return;
        }

        // Serve JavaScript
        if (path.equals("/script.js")) {

            sendFile(
                    exchange,
                    "script.js",
                    "application/javascript; charset=UTF-8"
            );

            return;
        }

        sendResponse(
                exchange,
                "404 - File Not Found",
                404
        );
    }


    // =========================
    // CALCULATE
    // =========================

    private static void calculate(HttpExchange exchange)
            throws IOException {

        String query =
                exchange.getRequestURI().getRawQuery();

        if (query == null || query.isEmpty()) {

            sendResponse(
                    exchange,
                    "Please provide numbers",
                    400
            );

            return;
        }

        double num1 = 0;
        double num2 = 0;
        String operator = "";

        String[] parameters = query.split("&");

        for (String parameter : parameters) {

            String[] pair = parameter.split("=", 2);

            if (pair.length != 2) {
                continue;
            }

            String key = pair[0];

            String value = URLDecoder.decode(
                    pair[1],
                    StandardCharsets.UTF_8
            );

            try {

                if (key.equals("num1")) {

                    num1 = Double.parseDouble(value);

                } else if (key.equals("num2")) {

                    num2 = Double.parseDouble(value);

                } else if (key.equals("operator")) {

                    operator = value;
                }

            } catch (NumberFormatException e) {

                sendResponse(
                        exchange,
                        "Invalid number",
                        400
                );

                return;
            }
        }


        // =========================
        // CALCULATION
        // =========================

        double result;

        switch (operator) {

            case "+":

                result = num1 + num2;
                break;


            case "-":

                result = num1 - num2;
                break;


            case "*":

                result = num1 * num2;
                break;


            case "/":

                if (num2 == 0) {

                    sendResponse(
                            exchange,
                            "Cannot divide by zero",
                            400
                    );

                    return;
                }

                result = num1 / num2;
                break;


            default:

                sendResponse(
                        exchange,
                        "Invalid operator",
                        400
                );

                return;
        }


        sendResponse(
                exchange,
                String.valueOf(result),
                200
        );
    }


    // =========================
    // SEND FILE
    // =========================

    private static void sendFile(
            HttpExchange exchange,
            String fileName,
            String contentType
    ) throws IOException {

        Path path = Path.of(fileName);

        if (!Files.exists(path)) {

            sendResponse(
                    exchange,
                    "File not found: " + fileName,
                    404
            );

            return;
        }

        byte[] bytes = Files.readAllBytes(path);

        exchange.getResponseHeaders()
                .set("Content-Type", contentType);

        exchange.sendResponseHeaders(
                200,
                bytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(bytes);
        }
    }


    // =========================
    // SEND RESPONSE
    // =========================

    private static void sendResponse(
            HttpExchange exchange,
            String response,
            int statusCode
    ) throws IOException {

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/plain; charset=UTF-8"
                );

        byte[] bytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(bytes);
        }
    }
}