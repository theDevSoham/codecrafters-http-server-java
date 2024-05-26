import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     ServerSocket serverSocket = null;
     Path baseDirectory = Paths.get(".");

     try {
       serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);

         while (true) {
             Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
             System.out.println("Accepted new connection");
             if (args[0].equals("--directory")) {
                 baseDirectory = Paths.get(args[1]).toAbsolutePath().normalize();
             }

             // Start a new thread to handle the client connection
             new Thread(new ClientHandler(clientSocket, baseDirectory)).start();
         }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
