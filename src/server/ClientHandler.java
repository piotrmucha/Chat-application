package server;

import messages.KindOfMessage;
import messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

class ClientHandler implements Runnable
{
    private String name;
    final ObjectInputStream dis;
    final ObjectOutputStream dos;
    Socket s;
    Boolean logged = true;

    public ClientHandler(Socket s,
                         ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                switch( received.getKindOfMessage() ){
                    case TRY_TO_CONNECT:  {
                        boolean flag=true;
                        for (ClientHandler mc : Server.ar)
                        {
                            if(received.getUserName().equals(mc.name)){
                                received.setKindOfMessage(KindOfMessage.DISCONNECTION);
                                this.dos.writeObject(received);
                                flag=false;
                                break;
                            }

                        }
                        if(flag==true&& Server.loginClients<10) {
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
                        }else if(flag==true && Server.loginClients >=10){
                            received.setKindOfMessage(KindOfMessage.USERS_LIMIT);
                            this.dos.writeObject(received);
                            Server.ar.remove(this);
                        }
                        break;
                    }
                    case CONNECTION:  {//proper nickname
                        this.setName(received.getUserName());
                        break;
                    }
                    case STANDARD_MESSAGE:  {
                        for (ClientHandler mc : Server.ar)
                        {
                            if(mc != this) {
                                mc.dos.writeObject(received);
                            }
                        }
                        break;
                    }
                    case DISCONNECTION: {
                        Server.loginClients--;
                        received.setUsersCounter(Server.loginClients);
                        for (ClientHandler mc : Server.ar)
                        {
                            if (mc!=this && mc.getName()!=null) {
                                mc.dos.writeObject(received);
                            }
                        }
                        Server.ar.remove(this);
                        logged = false;
                        break;
                    }
                    case SOFT_DISCONNETION: {
                        Server.ar.remove(this);
                        logged = false;
                        break;
                    }

                }
                if (logged == false) {
                    break;
                }
            }
            catch (Exception e) {
                    System.out.println("SocketException");
                    Server.loginClients--;
                    Message mess = new Message();
                    mess.setKindOfMessage(KindOfMessage.DISCONNECTION);
                    mess.setUsersCounter(Server.loginClients);

                    for (ClientHandler mc : Server.ar)
                    {
                    if (mc!=this && mc.getName()!=null) {
                        try {
                            mc.dos.writeObject(mess);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    }
                Server.ar.remove(this);
                try {
                    this.s.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }

        }
        try
        {
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
