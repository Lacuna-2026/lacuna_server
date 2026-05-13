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
    private boolean prv = false;

    public Session() {
        this(false);
    }

    public Session(boolean prv) {
        this.id = generateId();
        this.lastTouchedTime = System.currentTimeMillis();
        this.prv = prv;
    }

    public String getId() {
        return this.id;
    }

    public synchronized long getLastTouchedTime() {
        return this.lastTouchedTime;
    }

    public synchronized void touch() {
        this.lastTouchedTime = System.currentTimeMillis();
    }

    public synchronized void registerNewOwner(ClientConnexion owner) {
        this.touch();
        this.owner = owner;
    }

    public synchronized void unregisterOwner() {
        this.touch();
        this.owner = this.visitor;
        this.visitor = null;
    }

    public synchronized ClientConnexion getOwner() {
        return owner;
    }

    public synchronized ClientConnexion getVisitor() {
        return visitor;
    }

    public synchronized void registerVisitor(ClientConnexion visitor) {
        this.touch();
        this.visitor = visitor;
    }

    public synchronized void unregisterVisitor() {
        this.touch();
        this.visitor = null;
    }

    public synchronized void unregisterClient(ClientConnexion client) {
        this.touch();
        if (client == owner) {
            this.unregisterOwner();
        }
        if (client == visitor) {
            this.unregisterVisitor();
        }
    }

    public synchronized ClientConnexion getPeer(ClientConnexion client) {
        if (client == owner) {
            return visitor;
        }
        if (client == visitor) {
            return owner;
        }
        return null;
    }

    public synchronized boolean hasClient(ClientConnexion client) {
        return client == owner || client == visitor;
    }

    public synchronized boolean isFull() {
        return this.owner != null && this.visitor != null;
    }

    public synchronized boolean isPrv() {
        return this.prv;
    }

    public synchronized long getMinutesUntilOutdated() {
        long elapsedTime = System.currentTimeMillis() - this.lastTouchedTime;
        long remainingTime = OUTDATED_AFTER_MS - elapsedTime;

        if (remainingTime <= 0) {
            return 0;
        }

        return (remainingTime + 59999) / 60000;
    }

    public synchronized boolean isOutdated() {
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
