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
    private ClientConnexion currentClient;
    private Session currentSession;

    Connexion_handler(Socket clientSocket, SessionManager sessionManager) {
        this.clientSocket = clientSocket;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        boolean joinedSession = false;

        try {
            in = new BufferedReader(
                    new InputStreamReader(this.clientSocket.getInputStream()));

            out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            String line = in.readLine();
            joinedSession = handleFirstLine(line, in, out);

            if (joinedSession) {
                forwardMessages(currentSession, currentClient);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            if (joinedSession) {
                closeSession(currentSession, currentClient);
            } else {
                closeClientResources(in, out);
            }
        }
    }

    private boolean handleFirstLine(String line, BufferedReader in, PrintWriter out) {
        if (line == null || line.trim().isEmpty()) {
            out.println("BAD 400");
            return false;
        }

        String[] parts = line.trim().split("\\s+");
        String command = parts[0].toUpperCase(Locale.ROOT);

        switch (command) {
            case "PING":
                return handlePing(parts, out);
            case "CREAT":
                return handleCreate(parts, in, out);
            case "JOIN":
                return handleJoin(parts, in, out);
            default:
                out.println("BAD 404");
                return false;
        }
    }

    private boolean handlePing(String[] parts, PrintWriter out) {
        if (parts.length != 1) {
            out.println("BAD 400");
            return false;
        }

        out.println("pong");
        return false;
    }

    private boolean handleCreate(String[] parts, BufferedReader in, PrintWriter out) {
        if (parts.length != 2) {
            out.println("BAD 400");
            return false;
        }

        currentClient = new ClientConnexion(this.clientSocket, parts[1], in, out);
        currentSession = this.sessionManager.createSession(currentClient);
        out.println("OK " + currentSession.getId());
        return true;
    }

    private boolean handleJoin(String[] parts, BufferedReader in, PrintWriter out) {
        if (parts.length != 3) {
            out.println("BAD 400");
            return false;
        }

        String sessionId = parts[2].toUpperCase(Locale.ROOT);
        Session session = this.sessionManager.getSession(sessionId);

        if (session == null) {
            out.println("BAD 500");
            return false;
        }

        ClientConnexion client = new ClientConnexion(this.clientSocket, parts[1], in, out);

        synchronized (session) {
            if (session.getOwner() == null || session.isFull()) {
                out.println("BAD 405");
                return false;
            }

            session.registerVisitor(client);
        }

        currentClient = client;
        currentSession = session;
        out.println("OK");
        return true;
    }

    private void forwardMessages(Session session, ClientConnexion client) throws IOException {
        String commandLine;

        while ((commandLine = client.receiveFromClient()) != null) {
            String[] parts = commandLine.trim().split("\\s+");
            String command = parts[0].toUpperCase(Locale.ROOT);

            if ("LEAVE".equals(command)) {
                if (parts.length != 1) {
                    client.sendToClient("BAD 400");
                    continue;
                }

                ClientConnexion peer;

                synchronized (session) {
                    peer = session.getPeer(client);
                }

                if (peer != null) {
                    peer.sendToClient("LEFT");
                }

                return;
            }

            if (parts.length != 2 || !"DATA".equals(command)) {
                client.sendToClient("BAD 400");
                continue;
            }

            int lineCount;

            try {
                lineCount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                client.sendToClient("BAD 400");
                continue;
            }

            if (lineCount < 0) {
                client.sendToClient("BAD 400");
                continue;
            }

            ClientConnexion peer;

            synchronized (session) {
                peer = session.getPeer(client);
            }

            if (peer == null) {
                client.sendToClient("BAD 300");
                if (!skipDataLines(client, lineCount)) {
                    return;
                }
                continue;
            }

            if (!peer.sendToClient("DATA " + lineCount)) {
                return;
            }

            for (int i = 0; i < lineCount; i++) {
                String dataLine = client.receiveFromClient();

                if (dataLine == null || !peer.sendToClient(dataLine)) {
                    return;
                }
            }

            session.touch();
        }
    }

    private boolean skipDataLines(ClientConnexion client, int lineCount) throws IOException {
        for (int i = 0; i < lineCount; i++) {
            if (client.receiveFromClient() == null) {
                return false;
            }
        }

        return true;
    }

    // if the session is umpty then we delete it
    // if the one who left /lost connexion is an owner the new owner is the visitor
    // (handled by session manager)
    // if it was not a session then we just close everything
    private void closeSession(Session session, ClientConnexion client) {
        boolean sessionIsEmpty;

        synchronized (session) {
            session.unregisterClient(client);
            sessionIsEmpty = session.getOwner() == null && session.getVisitor() == null;
        }

        client.close();

        if (sessionIsEmpty) {
            this.sessionManager.deleteSession(session.getId());
        }
    }

    private void closeClientResources(BufferedReader in, PrintWriter out) {
        if (out != null) {
            out.close();
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client input: " + e.getMessage());
        }

        try {
            if (clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }

}
