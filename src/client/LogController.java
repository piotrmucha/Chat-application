package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import javafx.stage.Stage;
import messages.*;

public class LogController {
    @FXML
    private TextField username;
    @FXML
    private Button click;
    @FXML
    private TextFlow status;
    @FXML
    private AnchorPane stage;
    final static int ServerPort = 4998;
    static ObjectInputStream input;
    static ObjectOutputStream output;
    static Socket s;
    static String userN;
    static int userCounts = 0;

    public LogController() {
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


                    InetAddress ip = InetAddress.getByName("10.130.42.146");

                    s = new Socket(ip, ServerPort);


                    output = new ObjectOutputStream(s.getOutputStream());
                    input = new ObjectInputStream(s.getInputStream());


                    //    } catch (InterruptedException e) {
                    //       Thread.currentThread().interrupt();
                    // code for stopping current task so thread stops

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
        Thread connection = new Thread(task);
        connection.setDaemon(true);
        connection.start();


    }

    void loginAction() {
        Platform.runLater(() -> {

            try {
                Stage currentStage = (Stage) stage.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/client.fxml"));
                Parent content = loader.load();
                Scene scene = new Scene(content);
                currentStage.setScene(scene);
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    @FXML
    void loginClick() {
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                String user = username.getText();
                userN = user;
                if (user.equals("")) {

                } else if (user.length() < 2 || user.length() > 12) {
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
                        if (result.getKindOfMessage()!= KindOfMessage.USER_COUNTER) {
                            break;
                        }
                    }
                    KindOfMessage answer = result.getKindOfMessage();

                    if (answer == KindOfMessage.CONNECTION) {
                        userCounts = result.getUsersCounter();
                        loginAction();
                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Nie udało się ustawid pseudonimu. Wybrany pseudonim jest już zajęty.");
                            //     username.clear();
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
