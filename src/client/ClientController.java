package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
//import server.ClientHandler;
import server.ClientHandler;
import server.Server;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class ClientController {
    private DataInputStream sInput;        // to read from the socket
    private DataOutputStream sOutput;        // to write on the socket
    private Socket socket;
    private String nick, message;
    private int port;

    public void initialize(){
        port = 4999;
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("192.168.56.1");    //192.168.56.1
            Socket s = new Socket(ip, port);
            sInput = new DataInputStream(s.getInputStream());
            sOutput = new DataOutputStream(s.getOutputStream());
//            Vector<ClientHandler> e= Server.getVector();
//            System.out.println("abcd");
//            System.out.println(Server.getVector().size());
//            for(ClientHandler q:e){
//                System.out.println(q.getName());
//            }
//            System.out.println("abcd");
            nick = "PCPC";
            message = "Patryk";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputArea.setEditable(false);
        new ListenFromServer().start();

        // establish the connection
    }


    @FXML
    private TextArea outputArea;

    @FXML
    private TextArea messagesArea;

    @FXML
    private Button sendButton;

    @FXML
    void emoticon01Fun(ActionEvent event) {
        messagesArea.appendText("😀");
    }

    @FXML
    void emoticon02Fun(ActionEvent event) {
        messagesArea.appendText("🙄");
    }

    @FXML
    void emoticon03Fun(ActionEvent event) {
        messagesArea.appendText("😬");
    }

    @FXML
    void emoticon04Fun(ActionEvent event) {
        messagesArea.appendText( "😡");
    }

    @FXML
    void emoticon05Fun(ActionEvent event) {
        messagesArea.appendText("🤑");
    }

    @FXML
    void emoticon06Fun(ActionEvent event) {
        messagesArea.appendText("😇");
    }

    @FXML
    void emoticon07Fun(ActionEvent event) {
        messagesArea.appendText("😅");
    }

    @FXML
    void emoticon08Fun(ActionEvent event) {
        messagesArea.appendText("🤣");
    }

    @FXML
    void sendMessage(ActionEvent event) {
       String recived= messagesArea.getText();
       if(!recived.isEmpty()){
           try {
               sOutput.writeUTF(recived);
               messagesArea.setText("");
           } catch (IOException e) {
               System.out.println("Problem z wysyłaniem wiadomości");
               e.printStackTrace();
           }
       }
    }
//    void sendHyperlink(String s){
//        messagesArea.append;
//        return;
//    }
    
    private void display(String msg) {
        outputArea.appendText(msg + "\n"); // append to the ServerChatArea
    }

    class ListenFromServer extends Thread {

        public void run() {

            while (true) {
                try {
                    // read the message sent to this client
                    String msg = sInput.readUTF();
                    msg += "\n";
                    //  controller.OutputArea.appendText(msg);
                    outputArea.appendText(msg);
                    //  System.out.println(msg);
                } catch (SocketException t) {
                    try {
                        sOutput.close();
                        sInput.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outputArea.appendText("Utracono połączenie z serwerem, sprawdz połączenie internetowe \n");
                    break;
                } catch (SocketTimeoutException k) {
                    try {
                        sOutput.close();
                        sInput.close();
                        socket.close();
                    } catch (IOException e) {
                        k.printStackTrace();
                    }
                    outputArea.appendText("Zbyt  długi czas oczekiwania na połączenie z siecią internetową \n");
                    break;
                } catch (IOException e) {

                    e.printStackTrace();

                }
            }
        }

    }
}
