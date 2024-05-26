import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = getResponse(clientSocket);

            // Close streams and sockets
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static PrintWriter getResponse(Socket clientSocket) throws IOException {
        String responseBody = "Hello, world!";
        String errorBody = "Something went wrong!";

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Map<String, String> headers = new HashMap<String, String>();
        String requestLine = in.readLine();
        String headerLine;

        // Parse request line to extract header
        while((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
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

        if (urlPath.equals("/")) {
            // Get output stream of client socket
            return getPrintWriter(false, clientSocket, responseBody);
        }
        else if (urlPath.startsWith("/echo/")) {
            return getPrintWriter(false, clientSocket, urlPath.substring(6));
        }
        else if (urlPath.equals("/user-agent")) {
            return getPrintWriter(false, clientSocket, headers.get("User-Agent"));
        }
        else {
            // Get output stream of client socket
            return getPrintWriter(true, clientSocket, errorBody);
        }
    }

    private static PrintWriter getPrintWriter(Boolean failed, Socket clientSocket, String responseBody) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter out = new PrintWriter(outputStream, true);

        if (failed) {
            // Send http response to client
            out.print("HTTP/1.1 404 Not Found\r\n");
            out.print("Content-Type: text/plain\r\n");
            out.print("Content-Length: " + responseBody.length() + "\r\n");
            out.print("\r\n");
            out.print(responseBody);
        } else {
            // Send http response to client
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: text/plain\r\n");
            out.print("Content-Length: " + responseBody.length() + "\r\n");
            out.print("\r\n");
            out.print(responseBody);
        }

        // Ensure all data is sent by flushing the stream
        out.flush();
        return out;
    }
}
