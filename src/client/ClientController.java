package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import messages.KindOfMessage;
import messages.Message;
import java.io.*;
import java.net.*;

import static messages.KindOfMessage.*;

public class ClientController  {
    private ObjectInputStream sInput;        // to read from the socket
    private ObjectOutputStream sOutput;        // to write on the socket
    private Socket socket;
    private String nick;
    private int counter;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextArea messagesArea;

    @FXML
    private Button sendButton;

    @FXML
    private Text Counter;

    @FXML
    private Pane stage;

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

    public ClientController () {
        sInput = LogController.input;
        sOutput = LogController.output;
        socket = LogController.s;
        nick = LogController.userN;
        counter=LogController.userCounts;
    }
    public void initialize(){
        if(true != false){
            int r=312;
        }
        outputArea.setEditable(false);
        Counter.setText(Integer.toString(counter));
        new ListenFromServer().start();

    }

    @FXML
    void sendMessage(ActionEvent event) {
        String received= messagesArea.getText();
        int len=received.length();
        if(!received.isEmpty() && len<280){
            Message toSent= new Message();
            toSent.setUserName(nick);
            toSent.setKindOfMessage(STANDARD_MESSAGE);
            toSent.setContent(received);
            try {
                sOutput.writeObject(toSent);
                messagesArea.setText("");
            } catch (IOException e) {
                System.out.println("Problem z wysyłaniem wiadomości");
                e.printStackTrace();
            }
        } else if(len>=280){
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Nie udało się wysład wiadomości.\n" +
                        "Przekroczono limit znaków (280)");
                alert.showAndWait();
            });
        }
    }


    class ListenFromServer extends Thread {

        public void run() {

            Message received = null;
            while (true) {
                try {
                    // read the message sent to this client
                    received = (Message) sInput.readObject();

                    if(received.getKindOfMessage() == STANDARD_MESSAGE) {
                        String msg = received.getContent();
                        msg = received.getUserName() + ": " + msg;
                        msg += "\n";
                        outputArea.appendText(msg);
                    }
                    else if(received.getKindOfMessage() == USER_COUNTER) {
                        Counter.setText( Integer.toString( received.getUsersCounter() ) );
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
