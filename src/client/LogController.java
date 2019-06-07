package client;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import messages.KindOfMessage;
import messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class LogController {

    final static int ServerPort = 4998;
    static ObjectInputStream input;
    static ObjectOutputStream output;
    static Socket s;
    static String userN;
    static int userCounts = 0;

    private Stage thisStage;
    @FXML
    private TextField username;
    @FXML
    private Button click;
    @FXML
    private TextFlow status;
    @FXML
    private AnchorPane stage;


    public LogController() {
        thisStage=Main.primStage;
    }

    public void initialize() {
        // Platform.runLater(()-> {
        Text wait = new Text("Czekam na połączenie..");
        status.getChildren().add(wait);
        status.setTextAlignment(TextAlignment.CENTER);
        username.setDisable(true);
        click.setDisable(true);
        //  });
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    InetAddress ip = InetAddress.getByName("188.146.167.187");
                    s = new Socket(ip, ServerPort);
                    output = new ObjectOutputStream(s.getOutputStream());
                    input = new ObjectInputStream(s.getInputStream());
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Nie udało się nawiązac połączenia z serwerem. Sprawdź swoje połączenie internetowe.");
                        alert.showAndWait();
                        Platform.exit();
                        System.exit(0);
                    });
                }
                status.getChildren().clear();
                Text correct = new Text("Pomyślnie nawiązano połączenie z serwerem");
                status.getChildren().add(correct);
                username.setDisable(false);
                click.setDisable(false);
                return null;
            }
        };
        thisStage.setOnCloseRequest(e ->{
            if(s!=null && s.isConnected()==true) {
                Message exitMessage = new Message();
                exitMessage.setKindOfMessage(KindOfMessage.SOFT_DISCONNETION);
                try {
                    output.writeObject(exitMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                disconnect();
            }
            Platform.exit();
            System.exit(0);
        });
        Thread connection = new Thread(task);
        connection.setDaemon(true);
        connection.start();
    }
    private void disconnect(){
        try {
            input.close();
            output.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loginAction() {
        Platform.runLater(() -> {

            try {
                Stage currentStage = (Stage) stage.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/client.fxml"));
                Parent content = loader.load();
                Scene scene = new Scene(content);
                currentStage.setScene(scene);
                currentStage.setTitle("Okno czatu");
                currentStage.getIcons().add(new Image("resources/icon.png"));
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    @FXML
    void loginClick() {       //if we click button  Ustaw Pseudonim, stage starts listen
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                String user = username.getText();
                userN = user;
                if (user.equals("")) ;
                else if (user.length() < 2 || user.length() > 12) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Nie udało się ustawić pseudonimu. Liczba znaków musi być nie mniejsza od 2 i nie większa od 12");
                        //  username.clear();
                        alert.showAndWait();
                    });
                } else {
                    Message loginTry = new Message();
                    KindOfMessage logging = KindOfMessage.TRY_TO_CONNECT;
                    loginTry.setKindOfMessage(logging);
                    loginTry.setUserName(user);
                    try {
                        output.writeObject(loginTry);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Message result = null;
                    while (true) {
                        try {
                            result = (Message) input.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (result.getKindOfMessage()== KindOfMessage.CONNECTION || result.getKindOfMessage()==KindOfMessage.DISCONNECTION      ||
                                result.getKindOfMessage()==KindOfMessage.USERS_LIMIT ) {
                            break;
                        }
                    }
                    KindOfMessage answer = result.getKindOfMessage();
                    if (answer == KindOfMessage.CONNECTION) {
                        userCounts = result.getUsersCounter();
                        loginAction();
                    } else if  (answer == KindOfMessage.DISCONNECTION){
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Nie udało się ustawid pseudonimu. Wybrany pseudonim jest już zajęty.");
                            alert.showAndWait();
                        });
                    } else if (answer == KindOfMessage.USERS_LIMIT){
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Nie udało się dołączyć do chatroomu – limit uczestników konwersacji został osiągnięty." +
                                    "\n Spróbuj ponownie później.");
                            alert.showAndWait();
                        });
                    }
                }
                return null;
            }
        };
        Thread connection = new Thread(task);
        connection.setDaemon(true);
        connection.start();
    }
}
