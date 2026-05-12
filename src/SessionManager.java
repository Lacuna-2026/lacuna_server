package src;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session createSession(ClientConnexion owner) {
        cleanupOutdatedSessions();

        Session session;

        do {
            session = new Session();
            session.registerNewOwner(owner);
        } while (sessions.putIfAbsent(session.getId(), session) != null);

        return session;
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
