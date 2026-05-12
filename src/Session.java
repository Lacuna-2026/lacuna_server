package src;

import java.security.SecureRandom;

public class Session {
    // session id configs:
    private static final String ID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final long OUTDATED_AFTER_MS = 30 * 60 * 1000; // 30 min of idle at max

    private final String id;
    private long lastTouchedTime;
    private ClientConnexion owner;
    private ClientConnexion visitor;

    public Session() {
        this.id = generateId();
        this.lastTouchedTime = System.currentTimeMillis();
    }

    public String getId() {
        return this.id;
    }

    public long getLastTouchedTime() {
        return this.lastTouchedTime;
    }

    public void touch() {
        this.lastTouchedTime = System.currentTimeMillis();
    }

    public void registerNewOwner(ClientConnexion owner) {
        this.touch();
        this.owner = owner;
    }

    public void unregisterOwner() {
        this.touch();
        this.owner = this.visitor;
        this.visitor = null;
    }

    public ClientConnexion getOwner() {
        return owner;
    }

    public ClientConnexion getVisitor() {
        return visitor;
    }

    public void registerVisitor(ClientConnexion visitor) {
        this.touch();
        this.visitor = visitor;
    }

    public void unregisterVisitor() {
        this.touch();
        this.visitor = null;
    }

    public boolean isFull() {
        return this.owner != null && this.visitor != null;
    }

    public boolean isOutdated() {
        return System.currentTimeMillis() - this.lastTouchedTime > OUTDATED_AFTER_MS;
    }

    private static String generateId() {
        StringBuilder id = new StringBuilder(ID_LENGTH);

        for (int i = 0; i < ID_LENGTH; i++) {
            int index = RANDOM.nextInt(ID_CHARACTERS.length());
            id.append(ID_CHARACTERS.charAt(index));
        }

        return id.toString();
    }
}
