package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import messages.Message;
import javafx.scene.text.Font;


import java.awt.*;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;


import static messages.KindOfMessage.*;

public class ClientController {
    private ObjectInputStream sInput;        // to read from the socket
    private ObjectOutputStream sOutput;        // to write on the socket
    private Socket socket;
    private String nick;
    private int counter;
    private Stage thisStage;

    @FXML
    private TextFlow outputArea;

    @FXML
    private TextArea messagesArea;

    @FXML
    private Text Counter;
    @FXML
    private ScrollPane scroll;

    public ClientController () {
        sInput = LogController.input;
        sOutput = LogController.output;
        socket = LogController.s;
        nick = LogController.userN;
        counter=LogController.userCounts;
        thisStage=Main.primStage;
    }
    public void initialize(){
        scroll.vvalueProperty().bind(outputArea.heightProperty());
        Counter.setText(Integer.toString(counter));
        messagesArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {

                    sendMessage(new ActionEvent());
                }
            }
        });

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
    private boolean isValidURL(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception)
        {
            return false;
        }
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

                if(isValidURL(received)) {
                    final String correct = received;
                    Hyperlink link = new Hyperlink(received);
                    final String userPart = this.nick;
                    Text wait = new Text(userPart);
                    wait.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            try {
                                Desktop.getDesktop().browse(new URL(correct).toURI());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (URISyntaxException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    Platform.runLater(() -> {
                        outputArea.getChildren().add(wait);
                        outputArea.getChildren().add(link);
                        if (event.getSource() instanceof Button) {
                            outputArea.getChildren().add(new Text(System.lineSeparator()));
                        }
                    });
                }
                else {
                    Text nickArea = new Text(this.nick+": ");
                    nickArea.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                    Text message = new Text(received);
                    Platform.runLater(() -> {
                        outputArea.getChildren().add(nickArea);
                        outputArea.getChildren().add(message);
                        if (event.getSource() instanceof Button) {
                            outputArea.getChildren().add(new Text(System.lineSeparator()));
                        }
                    });

                }
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
              if(c>31 && c<123){
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
                        if(isValidURL(msg)) {
                            final String correct = msg;
                            Hyperlink link = new Hyperlink(msg);
                            Text nickArea = new Text(received.getUserName()+": ");
                            nickArea.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                            link.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent e) {
                                    try {
                                        Desktop.getDesktop().browse(new URL(correct).toURI());
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    } catch (URISyntaxException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                            Platform.runLater(() -> {
                                outputArea.getChildren().add(nickArea);
                                outputArea.getChildren().add(link);
                                outputArea.getChildren().add(new Text(System.lineSeparator()));
                                playMusic();
                            });
                        }
                        else {
                            Text nickArea = new Text(received.getUserName()+": ");
                            nickArea.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                            Text mesage = new Text(decrytp(received.getContent()));
                            Platform.runLater(() -> {
                                outputArea.getChildren().add(nickArea);
                                outputArea.getChildren().add(mesage);
                                outputArea.getChildren().add(new Text(System.lineSeparator()));
                                playMusic();
                            });
                        }

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
                //    outputArea.appendText("Utracono połączenie z serwerem, sprawdz połączenie internetowe \n");
                    break;
                } catch (SocketTimeoutException k) {
                    try {
                        sOutput.close();
                        sInput.close();
                        socket.close();
                    } catch (IOException e) {
                        k.printStackTrace();
                    }
                  //  outputArea.appendText("Zbyt  długi czas oczekiwania na połączenie z siecią internetową \n");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }

    }
}
