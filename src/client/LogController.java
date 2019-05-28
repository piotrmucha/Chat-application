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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import messages.*;



public class LogController  {
    @FXML private TextField username;
    @FXML private Button click;
    @FXML private TextFlow status;
    final static int ServerPort = 4999;
    public LogController(){}
    public void initialize () {
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

                    //  Thread.sleep(1000);
                    InetAddress ip = InetAddress.getByName("10.60.0.217");

                    // establish the connection

                    Socket s = new Socket(ip, ServerPort);

                    // obtaining input and out streams

                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    int b = 0;
                    //    } catch (InterruptedException e) {
                    //       Thread.currentThread().interrupt();
                    // code for stopping current task so thread stops

                } catch (Exception e) {
                    Platform.runLater(()-> {
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

    }
}
