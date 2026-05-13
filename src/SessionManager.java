package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session createSession(ClientConnexion owner) {
        return createSession(owner, false);
    }

    public Session createSession(ClientConnexion owner, boolean prv) {
        cleanupOutdatedSessions();

        Session session;

        do {
            session = new Session(prv);
            session.registerNewOwner(owner);
        } while (sessions.putIfAbsent(session.getId(), session) != null);

        return session;
    }

    public List<Session> listAvailableSessions() {
        cleanupOutdatedSessions();

        List<Session> availableSessions = new ArrayList<>();

        for (Session session : sessions.values()) {
            if (!session.isFull()
                    && !session.isPrv()
                    && !session.isOutdated()) {
                availableSessions.add(session);
            }
        }

        return availableSessions;
    }

    public List<Session> listSessions() {
        cleanupOutdatedSessions();
        return new ArrayList<>(sessions.values());
    }

    public Session getSession(String id) {
        if (id == null) {
            return null;
        }

        cleanupOutdatedSessions();

        Session session = sessions.get(id);

        if (session != null && session.isOutdated()) {
            sessions.remove(id, session);
            return null;
        }

        return session;
    }

    public boolean deleteSession(String id) {
        if (id == null) {
            return false;
        }

        return sessions.remove(id) != null;
    }

    private void cleanupOutdatedSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isOutdated());
    }
}
