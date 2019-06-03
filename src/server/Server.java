package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    static Vector<ClientHandler> ar = new Vector<>();
    static int loginClients = 0;
    private volatile static Server instance;
    private Server() {

    }

    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server();
                }
            }
        }

        return instance;
    }

    public static void main(String args[]) {
        Server serwer = Server.getInstance();
        try {
            serwer.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void run() throws IOException {
        // method to find proper ip for server
        String ip;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();

        }
        ServerSocket ss = new ServerSocket(4998, 1, InetAddress.getByName(ip));
        System.out.println("\r\nRunning Server: " +
                "Host=" + ss.getLocalSocketAddress() +
                " Port=" + ss.getLocalPort());

        Socket s;

        while (true) {
            s = ss.accept();
            System.out.println("Request for client received: " + s);
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
            System.out.println("Creating new thread for this client");
            ClientHandler mtch = new ClientHandler(s, dis, dos);
            Thread t = new Thread(mtch);
            System.out.println("Adding client for list");
            ar.add(mtch);
            t.start();
        }
    }
}

