package server;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

// Server class 
public class Server
{
    private volatile  static Server instance;
    
    // Vector to store active clients 
    static Vector<ClientHandler> ar = new Vector<>();

    // counter for clients 
    static int i = 0;
    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    try {
                        instance = new Server();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return instance;
    }
    public static void main(String args[]){
        Server serwer=Server.getInstance();
    }
    private Server() throws IOException
    {
        // server is listening on port 1234 
        ServerSocket ss = new ServerSocket(4999, 1, InetAddress.getLocalHost());
        System.out.println("\r\nRunning Server: " +
                "Host=" + ss.getLocalSocketAddress() +
                " Port=" + ss.getLocalPort());

        Socket s;

        // running infinite loop for getting 
        // client request 
        while (true)
        {
            // Accept the incoming request 
            s = ss.accept();

            System.out.println("New client request received : " + s);

            // obtain input and output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            System.out.println("Creating a new handler for this client...");

            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);

            // Create a new Thread with this object. 
            Thread t = new Thread(mtch);

            System.out.println("Adding this client to active client list");

            // add this client to active clients list 
            ar.add(mtch);

            // start the thread. 
            t.start();

            // increment i for new client. 
            // i is used for naming only, and can be replaced 
            // by any naming scheme 
            i++;

        }
    }
}

