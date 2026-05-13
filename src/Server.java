package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int DEFAULT_PORT = 38400;

    public static void main(String[] args) {
        int port;
        ServerSocket serverSocket = null;
        SessionManager sessionManager = new SessionManager();

        try {
            port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            Thread cmdLineThread = new Thread(new Server_command_line(sessionManager));
            cmdLineThread.start();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Connexion_handler clientHandler = new Connexion_handler(clientSocket, sessionManager);
                    Thread clientThread = new Thread(clientHandler);
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
}
