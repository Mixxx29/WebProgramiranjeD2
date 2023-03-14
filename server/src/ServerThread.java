import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {

    private Server server; // Server reference
    private Socket socket; // Client socket

    private BufferedReader in; // Client input stream
    private PrintWriter out; // Client output stream

    private String username;

    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        // Get client io streams
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized public void writeMessage(String message) {
        out.println(message);
    }

    private void disconnect() {
        try {
            if (socket != null) socket.close(); // Close server socket
            if (in != null) in.close(); // Close server input stream
            if (out != null) out.close(); // Close server output stream
        } catch (IOException e) {
            ConsoleManager.println("Error closing resources!");
        }

        // Notify connected clients
        server.broadcastMessage(null, username + " has left!");

        server.removeClient(this);
    }

    @Override
    public void run() {
        // Send port for console communication
        writeMessage(server.generatePort() + "");

        // Send welcoming message
        writeMessage("Welcome to Public Chat!");

        // Handle username
        do {
            // Request username
            if (username == null) writeMessage("Enter your username: ");
            else writeMessage("Username is taken! Enter new username: ");
            try {
                username = in.readLine();
            } catch (IOException e) {
                System.out.println("Client disconected!");
                break;
            }
        } while (server.getClient(username) != null);

        // Confirm username to client
        writeMessage(username + " has joined!");

        // Notify connected clients
        server.broadcastMessage(null, username + " has joined!");

        server.addClient(this); // Add client to list

        // Send previous messages
        for (String message : server.getMessages()) {
            writeMessage(message);
        }

        // Read from client
        String message;
        while (true) {
            try {
                message = in.readLine();
            } catch (IOException e) {
                System.out.println("Client disconected!");
                break;
            }

            // Client has disconnected
            if (message == null) {
                System.out.println("Client disconected!");
                break;
            }

            // Quit message
            if (message.equals("/quit")) {
                System.out.println("Client disconected!");
                break;
            }

            // Create new broadcasting thread
            server.broadcastMessage(socket, message);
        }

        disconnect();
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }
}
