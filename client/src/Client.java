import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class Client implements Runnable {

    private static final String HOST = "127.0.0.1"; // Server host
    private static final int PORT = 8080; // Server port

    private Socket socket; // Server socket

    private BufferedReader in; // Server input stream
    private PrintWriter out; // Server output stream

    private Socket consoleSocket; // Input console socket
    private BufferedReader consoleIn; // Input console stream

    private String username; // Client username

    public Client() {
        ConsoleManager.setTitle("Public Chat"); // Set console title
        ConsoleManager.setSize(80, 30); // Set console size
    }

    private void connect(String host, int port) throws IOException {
        // Connect to server
        socket = new Socket(host, port);

        // Get server input stream
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Get server output stream
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        // Get port from server for console communication
        int consolePort = Integer.parseInt(in.readLine());

        // Create input console
        createInputConsole(consolePort);

        // Start communication thread
        new Thread(this).start();

        // Read from server
        String message = "";
        while (true) {
            try {
                message = in.readLine(); // Read line from server
            } catch (IOException e) {
                break;
            }

            if (message.equals("/quit")) {
                ConsoleManager.println("Server offline!");
                break;
            }

            // Extract username
            if (username == null) {
                ConsoleManager.clearScreen();
                if (message.endsWith(" has joined!")) {
                    username = message.split(" has joined!")[0];
                    continue;
                }
            }

            // Print received message
            ConsoleManager.println(message);
        }

        disconnect();
    }

    private void createInputConsole(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) { // Create server socket
            ConsoleManager.execute("start cmd /k java InputConsole " + port); // Start console app
            consoleSocket = serverSocket.accept(); // Wait for console to connect
            consoleIn = new BufferedReader(new InputStreamReader(consoleSocket.getInputStream())); // Get console input stream
        } catch (IOException e) {
            ConsoleManager.println("Creating input console failed!");
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (socket != null) socket.close(); // Close server socket
            if (in != null) in.close(); // Close server input stream
            if (out != null) out.close(); // Close server output stream
            if (consoleSocket != null) consoleSocket.close(); // Close input console socket
            if (consoleIn != null) consoleIn.close(); // Close input console input stream
        } catch (IOException e) {
            ConsoleManager.println("Error closing resources!");
        }

        System.exit(0); // Close client
    }

    @Override
    public void run() {
        String message = "";
        while (true) {
            // Read from input console
            try {
                message = consoleIn.readLine();
            } catch (IOException e) {
                disconnect(); // Console app disconnected
            }

            // Console has disconnected
            if (message == null) {
                System.out.println("Console disconected!");
                disconnect();
            }

            if (message.equals("/quit")) disconnect(); // Console app disconnected

            // Get current time
            LocalDateTime dateTime = LocalDateTime.now();
            String timeString = "<" + dateTime.getHour() + ":" + dateTime.getMinute() + ">";

            // Print message on console
            ConsoleManager.println("You: " + message + " " + timeString);

            // Send message to server
            if (username == null) out.println(message);
            else out.println(username + ": " + message + " " + timeString);
        }
    }

    public static void main(String[] args) {
        try {
            new Client().connect(HOST, PORT);
        } catch (IOException e) {
            ConsoleManager.println("Failed to connect!");
        }
    }
}
