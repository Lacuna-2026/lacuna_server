package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnexion {

    private Socket clientSocket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String clientName;

    ClientConnexion(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    ClientConnexion(Socket clientSocket, String clientName) throws IOException {
        this.clientSocket = clientSocket;
        this.clientName = clientName;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    ClientConnexion(Socket clientSocket, String clientName, BufferedReader in, PrintWriter out) {
        this.clientSocket = clientSocket;
        this.clientName = clientName;
        this.in = in;
        this.out = out;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public boolean sendToClient(String message) {
        out.println(message);
        out.flush();
        return !out.checkError();
    }

    public String receiveFromClient() throws IOException {
        return in.readLine();
    }

    public void close() {
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
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }

}
