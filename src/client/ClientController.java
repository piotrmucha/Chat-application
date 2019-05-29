package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import messages.Message;
import java.io.*;
import java.net.*;

import static messages.KindOfMessage.STANDARD_MESSAGE;

public class ClientController {
    private ObjectInputStream sInput;        // to read from the socket
    private ObjectOutputStream sOutput;        // to write on the socket
    private Socket socket;
    private String nick;

    public ClientController () {
        sInput = LogController.input;
        sOutput = LogController.output;
        socket = LogController.s;
        nick = LogController.userN;
        if ( true == true) {
            int k = 0;
        }
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
        String received= messagesArea.getText();
        if(!received.isEmpty()){
            Message toSent= new Message();
            toSent.setUserName(nick);
            toSent.setKindOfMessage(STANDARD_MESSAGE);
            toSent.setContent(received);
            try {
                sOutput.writeObject(received);
                messagesArea.setText("");
            } catch (IOException e) {
                System.out.println("Problem z wysyłaniem wiadomości");
                e.printStackTrace();
            }
        }
    }

    class ListenFromServer extends Thread {

        public void run() {

            while (true) {
                try {
                    // read the message sent to this client
                    Message received = (Message) sInput.readObject();
                    if(received.getKindOfMessage() == STANDARD_MESSAGE) {
                        String msg = received.getContent();
                        msg = this.getName() + ": " + msg;
                        msg += "\n";
                        outputArea.appendText(msg);
                    }
                }catch(ClassNotFoundException e ){
                    e.printStackTrace();

                }
                catch (SocketException t) {
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
