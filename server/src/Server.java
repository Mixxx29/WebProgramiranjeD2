import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private final int MAX_MESSAGES = 100;

    private List<String> censoredWords;

    private volatile ArrayList<ServerThread> clients;
    private volatile ArrayList<String> messages;

    private volatile int portCounter;

    public Server() {
        clients = new ArrayList<>();
        messages = new ArrayList<>();

        censoredWords = new ArrayList<>();
        censoredWords.add("test curse");
        censoredWords.add("bad word");
        censoredWords.add("can't say");
    }

    synchronized public int generatePort() {
        return ++portCounter + 8100;
    }

    public void start(int port) {
        // Create server socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Waiting for new client...");
                Socket clientSocket = serverSocket.accept(); // Wait for new client
                System.out.println("New client connected!");

                // Create server thread to handle new client
                ServerThread serverThread = new ServerThread(this, clientSocket);
                new Thread(serverThread).start(); // Start thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void broadcastMessage(Socket clientSocket, String message) {
        // Censor message
        for (String word : censoredWords) {
            if (message.contains(word)) {
                String censored = word.toCharArray()[0] + "";
                censored += "*".repeat(word.length() - 2);
                censored += word.toCharArray()[word.length() - 1] + "";
                message = message.replace(word, censored);
            }
        }

        // Add message to list
        messages.add(message); // Add new message to list

        // Trim old message
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }

        // Create broadcasting thread
        new BroadcastThread(this, clientSocket, message, new ArrayList<>(clients));
    }

    public ServerThread getClient(String username) {
        for (ServerThread client : clients)
            if (client.getUsername().equals(username))
                return client;

        return null;
    }

    synchronized public void addClient(ServerThread client) {
        clients.add(client);
    }

    synchronized public void removeClient(ServerThread client) {
        clients.remove(client);
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public static void main(String[] args) {
        new Server().start(8080); // Start server
    }
}
