import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

       String responseBody = "Hello, world!";

       // Get output stream of client socket
         OutputStream outputStream = clientSocket.getOutputStream();
         PrintWriter out = new PrintWriter(outputStream, true);

         // Send http response to client
         out.print("HTTP/1.1 200 OK\r\n");
         out.print("Content-Type: text/plain\r\n");
         out.print("Content-Length: " + responseBody.length() + "\r\n");
         out.print("\r\n");
         out.print(responseBody);

         // Ensure all data is sent by flushing the stream
         out.flush();

         // Close streams and sockets
         out.close();
         clientSocket.close();
         serverSocket.close();

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
