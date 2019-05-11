package server;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor 
    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;
    }

    @Override
    public void run() {

        String received;
        while (true)
        {
            try
            {
                // receive the string 
                received = dis.readUTF();

                System.out.println(received);

                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
                System.out.println("kurwa2");
                // break the string into message and recipient part 
                StringTokenizer st = new StringTokenizer(received, "#");
                String MsgToSend = st.nextToken();
                String recipient = st.nextToken();
                System.out.println("kurwa1");
                // search for the recipient in the connected devices list. 
                // ar is the vector storing client of active users 
                for (ClientHandler mc : Server.ar)
                {
                    System.out.println("kurwakk");
                    // if the recipient is found, write on its 
                    // output stream 
                //    if (mc.name.equals(recipient) && mc.isloggedin==true)
                //    {
                        System.out.println("sas");
                        mc.dos.writeUTF(this.name+" : "+MsgToSend);
                    //    break;
                 //   }
                }
                System.out.println("kurwis");
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
                System.out.println("kurwa");
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