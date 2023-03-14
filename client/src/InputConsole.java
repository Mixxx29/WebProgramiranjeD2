import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class InputConsole {

    private Socket socket;
    private PrintWriter out;

    private Scanner scanner;

    public InputConsole() {
        ConsoleManager.setTitle("Input Console"); // Set console title
        ConsoleManager.setSize(80, 3); // Set console size

        scanner = new Scanner(System.in);
    }

    public void connect(String host, int port) throws InterruptedException {
        // Connect to server
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String message;
        while (true) {
            ConsoleManager.clearScreen();
            ConsoleManager.print("\n Type -> ");
            message = scanner.nextLine();
            out.println(message);
            if (message.equals("/quit")) break;
        }

        closeConnection();
        ConsoleManager.terminate(); // Terminate console
    }

    private void closeConnection() {
        try {
            socket.close(); // Close socket
            out.close(); // Close output stream
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Create new client and connect to server
        try {
            new InputConsole().connect("127.0.0.1", Integer.parseInt(args[0]));
        } catch (InterruptedException ignored) {

        }
    }
}
