// Java implementation for multithreaded chat client 
// Save file as Client.java
 package client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    final static int ServerPort = 4999;

    public static void main(String args[]) throws UnknownHostException, IOException
    {
        Scanner scn = new Scanner(System.in);

        // getting localhost ip 
        InetAddress ip = InetAddress.getByName("10.130.42.146");

        // establish the connection 
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams 
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        // sendMessage thread 
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver. 
                    String msg = scn.nextLine();

                    try {
                        // write on the output stream 
                        dos.writeUTF(msg);
                    }
                    catch(SocketException t){
                        try {
                            dos.close();
                            dis.close();
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Utracono połączenie z serwerem, sprawdz połączenie internetowe");
                        return;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // readMessage thread 
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client 
                        String msg = dis.readUTF();
                        System.out.println(msg);
                    }catch(SocketException t){
                        try {
                            dos.close();
                            dis.close();
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Utracono połączenie z serwerem, sprawdz połączenie internetowe");
                        break;
                    }  catch(SocketTimeoutException k) {
                        try {
                            dos.close();
                            dis.close();
                            s.close();
                        } catch (IOException e) {
                            k.printStackTrace();
                        }
                        System.out.println("Zbyt  długi czas oczekiwania na połączenie z siecią internetową");
                        break;
                    }
                    catch (IOException e) {

                        e.printStackTrace();

                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}
