import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PingPongServer {
    private static final int DEFAULT_PORT = 38400;

    public static void main(String[] args) {
        int port;
        ServerSocket serverSocket = null;

        try {
            port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread clientThread = new Thread(() -> handleClient(clientSocket));
                    clientThread.start();

                } catch (IOException e) {
                    System.err.println("Accept error: " + e.getMessage());
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("Port must be a number.");
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String line = in.readLine();

            if ("ping".equals(line)) {
                out.println("pong");
            } else {
                out.println("expected ping");
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }

                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client resources: " + e.getMessage());
            }
        }
    }
}
