package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

public class Connexion_handler implements Runnable {

    private final Socket clientSocket;
    private final SessionManager sessionManager;

    Connexion_handler(Socket clientSocket, SessionManager sessionManager) {
        this.clientSocket = clientSocket;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        boolean socketAssignedToSession = false;

        try {
            in = new BufferedReader(
                    new InputStreamReader(this.clientSocket.getInputStream()));

            out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            String line = in.readLine();
            socketAssignedToSession = handleFirstLine(line, out);

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            if (socketAssignedToSession) {
                return;
            }

            try {
                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }

                if (clientSocket != null) {
                    this.clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client resources: " + e.getMessage());
            }
        }
    }

    private boolean handleFirstLine(String line, PrintWriter out) {
        if (line == null || line.trim().isEmpty()) {
            out.println("BAD");
            return false;
        }

        String[] parts = line.trim().split("\\s+");
        String command = parts[0].toUpperCase(Locale.ROOT);

        switch (command) {
            case "PING":
                return handlePing(parts, out);
            case "CREAT":
                return handleCreate(parts, out);
            case "JOIN":
                return handleJoin(parts, out);
            default:
                out.println("BAD");
                return false;
        }
    }

    private boolean handlePing(String[] parts, PrintWriter out) {
        if (parts.length != 1) {
            out.println("BAD");
            return false;
        }

        out.println("pong");
        return false;
    }

    private boolean handleCreate(String[] parts, PrintWriter out) {
        if (parts.length != 2) {
            out.println("BAD");
            return false;
        }

        ClientConnexion client = new ClientConnexion(this.clientSocket, parts[1]);
        Session session = this.sessionManager.createSession(client);
        out.println("OK " + session.getId());
        return true;
    }

    private boolean handleJoin(String[] parts, PrintWriter out) {
        if (parts.length != 3) {
            out.println("BAD");
            return false;
        }

        String sessionId = parts[2].toUpperCase(Locale.ROOT);
        Session session = this.sessionManager.getSession(sessionId);

        if (session == null) {
            out.println("BAD");
            return false;
        }

        ClientConnexion client = new ClientConnexion(this.clientSocket, parts[1]);

        synchronized (session) {
            if (session.getOwner() == null || session.isFull()) {
                out.println("BAD");
                return false;
            }

            session.registerVisitor(client);
        }

        out.println("OK");
        return true;
    }

}
