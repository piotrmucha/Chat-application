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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // constructor
    public ClientHandler(Socket s, String name,
                         ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = null;
        this.s = s;
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
                        System.out.println("TRY_TO_CONNECT");
                        for (ClientHandler mc : Server.ar)
                        {
                            if(received.getUserName().equals(mc.name)){
                                received.setKindOfMessage(KindOfMessage.DISCONNECTION);
                                this.dos.writeObject(received);
                                flag=false;
                                break;
                            }

                        }
                        if(flag==true) {
                            received.setKindOfMessage(KindOfMessage.CONNECTION);
                            this.setName(received.getUserName());
                            dos.writeObject(received);
                            int k=0;
                        }
                        break;
                    }
                    case CONNECTION:  {//proper nickname
                        System.out.println("CONNECTION");
                        this.setName(received.getUserName());
                        break;
                    }
                    case STANDARD_MESSAGE:  {
                        for (ClientHandler mc : Server.ar)
                        {
                            System.out.println("STANDARD_MESSAGE");
                            mc.dos.writeObject(received);
                        }
                        break;
                    }
                    case DISCONNECTION: {
                        //service later
                        System.out.println("DISCONNECTION");
                        break;
                    }

                }
                System.out.println("kos");
                System.out.println("jeden: "+this.getName()+" dwa: "+this.name);
                for (ClientHandler mc : Server.ar)
                {
                    System.out.println("sas");
                    System.out.println("Oto: "+mc.getName());
                }
                //System.out.println(received.getContent());

                /*if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }*/
                // break the string into message and recipient part 
        /*        StringTokenizer st = new StringTokenizer(received.getContent(), "#");
                String MsgToSend = st.nextToken();
                String recipient = st.nextToken();*/
                // search for the recipient in the connected devices list. 
                // ar is the vector storing client of active users 
                /*for (ClientHandler mc : Server.ar)
                {
                        System.out.println("sas");
                        mc.dos.writeUTF(this.name+" : "+MsgToSend);
                }*/
            }
            catch (SocketException e) {
                System.out.println("User disconnect from the server");
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
