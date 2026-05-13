package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class Server_command_line implements Runnable {

    private final SessionManager sessionManager;

    Server_command_line(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        System.out.println("Server command line is on ...");

        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
            String line;

            while (true) {
                System.out.print("Server > ");
                line = input.readLine();

                if (line == null) {
                    return;
                }

                handleCommand(line);
            }
        } catch (IOException e) {
            System.err.println("Command line error: " + e.getMessage());
        }
    }

    private void handleCommand(String line) {
        String[] parts = parseCommand(line);

        if (parts.length == 0) {
            return;
        }

        String command = parts[0].toUpperCase(Locale.ROOT);

        switch (command) {
            case "HELP":
                handleHelp(parts);
                break;
            case "LIST":
                handleList(parts);
                break;
            default:
                System.out.println("Unknown command. Type HELP.");
                break;
        }
    }

    private String[] parseCommand(String line) {
        String trimmedLine = line.trim();

        if (trimmedLine.isEmpty()) {
            return new String[0];
        }

        return trimmedLine.split("\\s+");
    }

    private void handleHelp(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Usage: HELP");
            return;
        }

        System.out.println("Commands:");
        System.out.println("  HELP - show this help");
        System.out.println("  LIST - show all active sessions");
    }

    private void handleList(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Usage: LIST");
            return;
        }

        List<Session> sessions = sessionManager.listSessions();

        if (sessions.isEmpty()) {
            System.out.println("No active sessions.");
            return;
        }

        System.out.printf("%-8s %-16s %-16s %-8s %-12s%n",
                "ID", "OWNER", "VISITOR", "PRV", "IDLE LEFT");
        System.out.printf("%-8s %-16s %-16s %-8s %-12s%n",
                "--------", "----------------", "----------------", "--------", "------------");

        for (Session session : sessions) {
            synchronized (session) {
                ClientConnexion owner = session.getOwner();
                ClientConnexion visitor = session.getVisitor();

                System.out.printf("%-8s %-16s %-16s %-8s %-12s%n",
                        session.getId(),
                        getClientName(owner),
                        getClientName(visitor),
                        session.isPrv() ? "yes" : "no",
                        session.getMinutesUntilOutdated() + " min");
            }
        }
    }

    private String getClientName(ClientConnexion client) {
        if (client == null || client.getClientName() == null) {
            return "-";
        }

        return client.getClientName();
    }
}
