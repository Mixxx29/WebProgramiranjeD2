import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

public class BroadcastThread implements Runnable {

    private static volatile Semaphore broadcastLock;

    static {
        broadcastLock = new Semaphore(1);
    }

    private Server server; // Server reference
    private Socket socket; // Client socket
    private String message; // Client message
    private List<ServerThread> clients; // Connected clients

    public BroadcastThread(Server server, Socket socket, String message, List<ServerThread> clients) {
        this.server = server;
        this.socket = socket;
        this.message = message;
        this.clients = clients;

        new Thread(this).start();
    }

    @Override
    public void run() {
        // Access broadcast lock
        try {
            broadcastLock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Broadcast to connected clients
        for (ServerThread client : clients) {
            if (client.getSocket() == socket) continue;
            client.writeMessage(message);
        }

        // Release lock
        broadcastLock.release();
    }
}
