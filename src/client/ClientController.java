package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import messages.Message;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.Map;

import static messages.KindOfMessage.*;

public class ClientController {
    private ObjectInputStream sInput;        // to read from the socket
    private ObjectOutputStream sOutput;        // to write on the socket
    private Socket socket;
    private String nick;
    private int counter;
    private Stage thisStage;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextArea messagesArea;

    @FXML
    private Text Counter;

    public ClientController () {
        sInput = LogController.input;
        sOutput = LogController.output;
        socket = LogController.s;
        nick = LogController.userN;
        counter=LogController.userCounts;
        thisStage=Main.primStage;
    }
    public void initialize(){
        outputArea.setEditable(false);
        Counter.setText(Integer.toString(counter));
        Thread  r = new ListenFromServer();
        r.setDaemon(true);
        r.start();
        thisStage.setOnCloseRequest(e ->{
            Message exitMessage = new Message();
            exitMessage.setKindOfMessage(DISCONNECTION);
            try {
                sOutput.writeObject(exitMessage);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Platform.exit();

            System.exit(0);
        });
    }

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
        int len=received.length();
        if(!received.isEmpty() && len<280){
            String encrypted=encrypt(received);//Cesar algorithm
            Message toSent= new Message();
            toSent.setUserName(nick);
            toSent.setKindOfMessage(STANDARD_MESSAGE);
            toSent.setContent(encrypted);
            String text=this.nick+": " +received+"\n";
            this.outputArea.appendText(text);
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

    public void playMusic(){
           try{
               String path= "messengerVoice.wav";
               AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path).getAbsoluteFile());
               Clip clip = AudioSystem.getClip();
               clip.open(audioInputStream);
               clip.start();
           } catch (UnsupportedAudioFileException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           } catch (LineUnavailableException e) {
               e.printStackTrace();
           }
    }

     //encrypting/decrypting only chars between range 32/122
    public String encrypt(String text){
          StringBuilder sb = new StringBuilder(text);
          int distance=7;
          for(int i=0;i<sb.length();i++){
              int c =(int) sb.charAt(i);
              if(c>31 && c<122){
               if(c+distance>122){
                   c = 31 + (distance -(122-c));
               }else{
                   c += distance;
               }
               sb.setCharAt(i,(char)c);
              }
          }
        return sb.toString();
    }

    public  String decrytp(String text){
        StringBuilder sb =new StringBuilder(text);
        int distance=7;
        for(int i=0;i<sb.length();i++){
            int c = (int) sb.charAt(i);
            if(c>31 && c<123){
                if(c-distance<32){
                    c = 123 - (32-(c-distance)) ;
                }   else{
                    c -=distance;
                }
                sb.setCharAt(i,(char)c);
            }
        }
        return sb.toString();
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
                        msg = decrytp(msg);
                        msg = received.getUserName() + ": " + msg + "\n";
                        playMusic();
                        outputArea.appendText(msg);
                    }                                                              
                    else if(received.getKindOfMessage() == USER_COUNTER) {
                        Counter.setText( Integer.toString( received.getUsersCounter() ) );
                    } else if(received.getKindOfMessage() == DISCONNECTION){
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
