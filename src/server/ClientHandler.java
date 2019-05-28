package server;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 

import messages.KindOfMessage;
import messages.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.StringTokenizer;

// ClientHandler class
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    private String name;
    final ObjectInputStream dis;
    final ObjectOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor 
    public ClientHandler(Socket s, String name,
                         ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;
    }

    @Override
    public void run() {

        Message received = null;
        while (true)
        {
            try
            {
                // receive the string
                try {
                    received = (Message) dis.readObject();
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                }
                switch(received.getKindOfMessage()){
                    case TRY_TO_CONNECT:  {
                        boolean flag=true;
                        for (ClientHandler mc : Server.ar)
                        {
                            if(received.getUserName().equals(mc.name)){
                                received.setKindOfMessage(KindOfMessage.DISCONNECTION);
                                dos.writeObject(received);
                                flag=false;
                                break;
                            }

                        }
                        if(flag==true) {
                            received.setKindOfMessage(KindOfMessage.CONNECTION);
                            dos.writeObject(received);
                        }
                        break;
                    }

                }

                //System.out.println(received.getContent());

                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
                // break the string into message and recipient part 
                StringTokenizer st = new StringTokenizer(received.getContent(), "#");
                String MsgToSend = st.nextToken();
                String recipient = st.nextToken();
                // search for the recipient in the connected devices list. 
                // ar is the vector storing client of active users 
                for (ClientHandler mc : Server.ar)
                {
                        System.out.println("sas");
                        mc.dos.writeUTF(this.name+" : "+MsgToSend);
                }
            }
            catch (SocketException e) {
                System.out.println("User disconnect from the server");
                this.isloggedin=false;
                try {
                    this.s.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        try
        {
            // closing resources 
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
