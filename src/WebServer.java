import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// REST API + serves the frontend folder. Run this to connect frontend and backend.
public class WebServer {

    private static final int PORT = 8080;
    private static final Path FRONTEND_DIR = Paths.get("..", "frontend").toAbsolutePath().normalize();
    private static final StudentStore store = new StudentStore();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/students", WebServer::handleStudents);
        server.createContext("/", WebServer::handleStatic);
        server.start();
        System.out.println("Server running. Open http://localhost:" + PORT + " in your browser.");
        System.out.println("Press Ctrl + C to stop.");
    }

    // ---------- API: /students ----------
    @SuppressWarnings("ConvertToTryWithResources")
    private static void handleStudents(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);
        String method = ex.getRequestMethod();

        if (method.equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return;
        }

        String path = ex.getRequestURI().getPath();   // /students or /students/ES24CD30

        if (method.equals("GET") && path.equals("/students")) {
            ArrayList<Student> all = store.getAll();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < all.size(); i++) {
                if (i > 0) json.append(",");
                json.append(toJson(all.get(i)));
            }
            json.append("]");
            sendJson(ex, 200, json.toString());

        } else if (method.equals("GET") && path.startsWith("/students/")) {
            String roll = java.net.URLDecoder.decode(path.substring("/students/".length()), StandardCharsets.UTF_8);
            Student s = store.findByRoll(roll);
            if (s == null) {
                sendJson(ex, 404, "{\"error\":\"Student not found\"}");
            } else {
                sendJson(ex, 200, toJson(s));
            }

        } else if (method.equals("POST") && path.equals("/students")) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String name = readField(body, "name");
            String roll = readField(body, "rollNumber");
            if (name.isEmpty() || roll.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"Name and roll number are required\"}");
            } else if (store.add(name.replace(",", " "), roll.replace(",", ""))) {
                sendJson(ex, 201, "{\"message\":\"Student added\"}");
            } else {
                sendJson(ex, 409, "{\"error\":\"Roll number already exists\"}");
            }

        } else {
            sendJson(ex, 405, "{\"error\":\"Not supported\"}");
        }
    }

    // ---------- Serve index.html, style.css, script.js ----------
    private static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        Path file = FRONTEND_DIR.resolve(path.substring(1)).normalize();
        if (!file.startsWith(FRONTEND_DIR) || !Files.isRegularFile(file)) {
            byte[] msg = "404 Not Found".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(404, msg.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(msg); }
            return;
        }

        String name = file.getFileName().toString();
        String type = name.endsWith(".html") ? "text/html; charset=UTF-8"
                    : name.endsWith(".css")  ? "text/css; charset=UTF-8"
                    : name.endsWith(".js")   ? "application/javascript; charset=UTF-8"
                    : "application/octet-stream";
        byte[] data = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    // ---------- Helpers ----------
    private static void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static String toJson(Student s) {
        return "{\"name\":\"" + escape(s.getName()) + "\",\"rollNumber\":\"" + escape(s.getRollNumber()) + "\"}";
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Reads a simple string field out of a JSON body like {"name":"Asha","rollNumber":"1"}
    private static String readField(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!m.find()) return "";
        return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\").trim();
    }
}
