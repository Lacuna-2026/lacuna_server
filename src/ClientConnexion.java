package src;

import java.net.Socket;

public class ClientConnexion {

    private Socket clientSocket = null;
    private String clientName;

    ClientConnexion(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    ClientConnexion(Socket clientSocket, String clientName) {
        this.clientSocket = clientSocket;
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // i dont know how yet but these needs to be present
    public void sendToClient() {
    }

    public void receiveFromClient() {
    }

}
