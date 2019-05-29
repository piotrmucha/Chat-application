package server;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
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
                        instance = new Server();
                }
            }
        }

        return instance;
    }
    public static void main(String args[]){
        Server serwer=Server.getInstance();
        try {
            serwer.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private Server(){
        
    }
    private void run() throws IOException
    {
        String ip;
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
             ip = socket.getLocalAddress().getHostAddress();

        }
        System.out.println(ip);
        // server is listening on port 1234
        String address = InetAddress.getLocalHost().getHostAddress()   ;
        System.out.println(address);
        ServerSocket ss = new ServerSocket(4999, 1, InetAddress.getByName(ip));
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
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());

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

