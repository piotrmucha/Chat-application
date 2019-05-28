package client;

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

import messages.*;


public class LogController {
    @FXML
    private TextField username;
    @FXML
    private Button click;
    @FXML
    private TextFlow status;
    final static int ServerPort = 4999;
    private ObjectInputStream input;
    private ObjectOutputStream output;

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


                    InetAddress ip = InetAddress.getByName("192.168.0.15");

                    Socket s = new Socket(ip, ServerPort);


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

    @FXML
    void loginClick() {
        String user = username.getText();
        if (user.equals("")) {
            return;
        }
        if (user.length() < 2 || user.length() > 12) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Nie udało się ustawić pseudonimu. Liczba znaków musi być nie mniejsza od 2 i nie większa od 12");
            username.clear();
            alert.showAndWait();
            return;
        }
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
        try {
             result = (Message)input.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        int k = 0;

    }
}
