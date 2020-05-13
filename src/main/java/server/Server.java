package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server {

    private volatile static Server instance;
    private static int THREAD_POOL_SIZE = 20;
    private static Logger logger = LogManager.getLogger(Server.class);
    private int port;
    private int loggedClients;
    private String hostAddress;
    private Collection<ClientHandler> clientHandlers;
    private ExecutorService executorService;


    private Server(int port, String hostAddress) {
        this.port = port;
        this.hostAddress = hostAddress;
        clientHandlers = new Vector<>();
        loggedClients = 0;
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    static Server getInstance(int port, String hostAddress) {
        if (instance == null) {
            synchronized (ServerApp.class) {
                if (instance == null) {
                    instance = new Server(port, hostAddress);
                }
            }
        }
        return instance;
    }

    static Server getInstance() {
        if (instance == null) {
            synchronized (ServerApp.class) {
                if (instance == null) {
                    instance = new Server(4998, "192.168.1.12");
                }
            }
        }
        return instance;
    }

    void run() throws IOException {

        ServerSocket serverSocket = new ServerSocket(port, 1, InetAddress.getByName(hostAddress));
        logger.info("Running ServerApp: Host= {} Port= {}", serverSocket.getLocalSocketAddress(), serverSocket.getLocalPort());
        waitForSocketConnection(serverSocket);
    }

    private void waitForSocketConnection(ServerSocket serverSocket) throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            logger.info("Request for client received: {}", socket);
            ObjectInputStream dis = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(socket.getOutputStream());
            ClientHandler newClient = new ClientHandler(socket, dis, dos);
            logger.info("Creating new thread for client={}", newClient);
            executorService.submit(new Thread(newClient));
            clientHandlers.add(newClient);
        }
    }

    Collection<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    int getLoggedClients() {
        return loggedClients;
    }

    void incrementLoggedClients() {
        this.loggedClients++;
    }

    void decrementLoggedClients() {
        this.loggedClients--;
    }
}
