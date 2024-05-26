import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

enum ResponseType {
    SUCCESS, FAILURE, FILE
}

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            handleRequest(clientSocket);
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        String responseBody = "Hello, world!";
        String errorBody = "Something went wrong!";

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Map<String, String> headers = new HashMap<>();
        String requestLine = in.readLine();
        String headerLine;

        // Parse request headers
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1].trim());
            }
        }

        // Parse the request line to extract the URL path
        String urlPath = "";
        if (requestLine != null && !requestLine.isEmpty()) {
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length > 1) {
                urlPath = requestParts[1];
            }
        }

        switch (urlPath) {
            case "/":
                sendResponse(ResponseType.SUCCESS, clientSocket, responseBody.getBytes(), "text/plain");
                break;
            case String p when p.startsWith("/echo/"):
                sendResponse(ResponseType.SUCCESS, clientSocket, urlPath.substring(6).getBytes(), "text/plain");
                break;
            case String p when p.startsWith("/user-agent"):
                sendResponse(ResponseType.SUCCESS, clientSocket, headers.getOrDefault("User-Agent", "Unknown User-Agent").getBytes(), "text/plain");
                break;
            case String p when p.startsWith("/files/"):
                Path filePath = Paths.get(urlPath.substring(7));
                if (Files.exists(filePath)) {
                    byte[] fileResponseBody = Files.readAllBytes(filePath);
                    sendResponse(ResponseType.FILE, clientSocket, fileResponseBody, "application/octet-stream");
                } else {
                    errorBody = "Error: File not found";
                    sendResponse(ResponseType.FAILURE, clientSocket, errorBody.getBytes(), "text/plain");
                }
                break;
            default:
                sendResponse(ResponseType.FAILURE, clientSocket, errorBody.getBytes(), "text/plain");
                break;
        }
    }

    private void sendResponse(ResponseType responseType, Socket clientSocket, byte[] responseBody, String contentType) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter out = new PrintWriter(outputStream, true);

        switch (responseType) {
            case SUCCESS:
                out.print("HTTP/1.1 200 OK\r\n");
                break;
            case FAILURE:
                out.print("HTTP/1.1 404 Not Found\r\n");
                break;
            case FILE:
                out.print("HTTP/1.1 200 OK\r\n");
                contentType = "application/octet-stream";
                break;
        }

        out.print("Content-Type: " + contentType + "\r\n");
        out.print("Content-Length: " + responseBody.length + "\r\n");
        out.print("\r\n");
        out.flush();

        outputStream.write(responseBody);
        outputStream.flush();
    }
}
