import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class SocketConnectionPool {
    private final Queue<Socket> pool;
    private final String serverIP;
    private final int serverPort;
    private final int poolSize;

    public SocketConnectionPool(String serverIP, int serverPort, int poolSize) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.poolSize = poolSize;
        this.pool = new LinkedList<>();

        initializePool();
    }

    private void initializePool() {
        try {
            for (int i = 0; i < poolSize; i++) {
                pool.add(new Socket(serverIP, serverPort));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing the connection pool", e);
        }
    }

    public synchronized Socket borrowSocket() {
        if (pool.isEmpty()) {
            throw new RuntimeException("No available connections in the pool");
        }
        return pool.poll();
    }

    public synchronized void returnSocket(Socket socket) {
        if (pool.size() < poolSize) {
            pool.add(socket);
        } else {
            closeSocket(socket);
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (Exception e) {
            // Handle exception
        }
    }
}
