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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// ClientHandler class
class ClientHandler implements Runnable
{
    private String name;
    final ObjectInputStream dis;
    final ObjectOutputStream dos;
    Socket s;
    Boolean logged = true;
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
                            Server.loginClients++;
                            received.setUsersCounter(Server.loginClients);
                            this.setName(received.getUserName());
                            dos.writeObject(received);
                            received.setKindOfMessage(KindOfMessage.USER_COUNTER);
                            for (ClientHandler mc : Server.ar)
                            {
                                if (mc!=this) {
                                    mc.dos.writeObject(received);
                                }
                            }
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
                        Server.loginClients--;
                        received.setUsersCounter(Server.loginClients);
                        for (ClientHandler mc : Server.ar)
                        {
                            if (mc!=this) {
                                mc.dos.writeObject(received);
                            }
                        }
                        Server.ar.remove(this);
                        logged = false;
                        break;
                    }

                }
                if (logged == false) {
                    break;
                }
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
