import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
       serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");

         PrintWriter out = getResponse(clientSocket);

         // Close streams and sockets
         out.close();
         clientSocket.close();
         serverSocket.close();

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }

    private static PrintWriter getResponse(Socket clientSocket) throws IOException {
        String responseBody = "Hello, world!";
        String errorBody = "Something went wrong!";

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String requestLine = in.readLine();

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
