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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Properties;

public class LogController {

    static Socket socket;
    static String userName;
    static int userCounts = 0;
    static ObjectInputStream input;
    static ObjectOutputStream output;
    private static String PROPERTIES_PATH = "config.properties";

    private static Logger LOGGER = LogManager.getLogger(LogController.class);

    private int serverPort;
    private Stage thisStage;
    private String hostAddress;

    @FXML
    private Button click;
    @FXML
    private TextFlow status;
    @FXML
    private AnchorPane stage;
    @FXML
    private TextField username;


    public LogController() {
        thisStage = Main.primStage;
    }

    private void exitApp() {
        Platform.runLater(() -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void initialize() {
        assignServerPropertiesFromFile();
        setupWindowWhileConnecting();
        setupOnCloseRequest();
        setupDaemonBackgroundTask();
    }

    private void assignServerPropertiesFromFile() {
        try {
            Properties properties = new Properties();
            properties.load(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(PROPERTIES_PATH)));
            serverPort = Integer.parseInt(properties.getProperty("port"));
            hostAddress = properties.getProperty("hostAddress");
        } catch (IOException exc) {
            LOGGER.error("Cannot read values from properties file");
            exc.printStackTrace();
        }
    }

    private void setupWindowWhileConnecting() {
        Text wait = new Text("Czekam na połączenie...");
        status.getChildren().add(wait);
        status.setTextAlignment(TextAlignment.CENTER);
        username.setDisable(true);
        click.setDisable(true);
    }

    private void setupOnCloseRequest() {
        thisStage.setOnCloseRequest(e -> {
            if (socket != null && socket.isConnected()) {
                Message exitMessage = new Message();
                exitMessage.setKindOfMessage(KindOfMessage.SOFT_DISCONNETION);
                trySentMessage(exitMessage);
                disconnect();
            }
            Platform.exit();
            System.exit(0);
        });
    }


    private void setupDaemonBackgroundTask() {
        Task task = getTask();
        runTaskAsThread(task);
    }

    private Task getTask() {
        return new Task<Void>() {
            @Override
            public Void call() {
                try {
                    LOGGER.info("serverPort = {}, hostAddress = {}", hostAddress, hostAddress);
                    InetAddress ip = InetAddress.getByName(hostAddress);
                    socket = new Socket(ip, serverPort);
                    output = new ObjectOutputStream(socket.getOutputStream());
                    input = new ObjectInputStream(socket.getInputStream());
                    displaySuccessfullyLoggedWindow();
                } catch (Exception e) {
                    displayFailedLoginWindow();
                }
                return null;
            }
        };
    }

    private void displaySuccessfullyLoggedWindow() {
        Platform.runLater(() -> {
            status.getChildren().clear();
            Text correct = new Text("Pomyślnie nawiązano połączenie z serwerem");
            status.getChildren().add(correct);
            username.setDisable(false);
            click.setDisable(false);
        });
    }

    private void displayFailedLoginWindow() {
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

    private void disconnect() {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (Exception e) {
            exitApp();
            e.printStackTrace();
        }
    }

    @FXML
    void loginClick() {
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                userName = username.getText();
                if (userName.equals("")) {
                    return null;
                } else if (userName.length() < 2 || userName.length() > 12) {
                    invalidUserLoginWindow("Nie udało się ustawić pseudonimu. Liczba znaków musi być nie mniejsza od 2 i nie większa od 12");
                } else {
                    Message tryToConnectMessage = createTryToConnectMessage(userName);
                    trySentMessage(tryToConnectMessage);
                    Message result = waitUntilProperTypeMessageReceived();
                    KindOfMessage answer = result.getKindOfMessage();
                    if (answer == KindOfMessage.CONNECTION) {
                        userCounts = result.getUsersCounter();
                        loginAction();
                    } else if (answer == KindOfMessage.DISCONNECTION) {
                        invalidUserLoginWindow("Nie udało się ustawid pseudonimu. Wybrany pseudonim jest już zajęty.");
                    } else if (answer == KindOfMessage.USERS_LIMIT) {
                        invalidUserLoginWindow("Nie udało się dołączyć do chatroomu – limit uczestników konwersacji został osiągnięty. \n Spróbuj ponownie później.");
                    }
                }
                return null;
            }
        };
        runTaskAsThread(task);
    }

    private void runTaskAsThread(Task task) {
        Thread connection = new Thread(task);
        connection.setDaemon(true);
        connection.start();
    }

    private Message waitUntilProperTypeMessageReceived() {
        Message result = null;
        while (true) {
            result = tryParseInputDataToMessage(result);
            if (expectedMessageType(result)) break;
        }
        return result;
    }

    private boolean expectedMessageType(Message result) {
        return result.getKindOfMessage() == KindOfMessage.CONNECTION ||
                result.getKindOfMessage() == KindOfMessage.DISCONNECTION ||
                result.getKindOfMessage() == KindOfMessage.USERS_LIMIT;
    }

    private Message tryParseInputDataToMessage(Message result) {
        try {
            result = (Message) input.readObject();
            if (result == null) throw new ClassNotFoundException("no input data provided, parsed to null");
        } catch (IOException | ClassNotFoundException exc) {
            exitApp();
            exc.printStackTrace();
        }
        return result;
    }

    private void trySentMessage(Message tryToConnectMessage) {
        try {
            output.writeObject(tryToConnectMessage);
        } catch (IOException e) {
            exitApp();
            e.printStackTrace();
        }
    }

    private Message createTryToConnectMessage(String userLogin) {
        Message loginTry = new Message();
        loginTry.setKindOfMessage(KindOfMessage.TRY_TO_CONNECT);
        loginTry.setUserName(userLogin);
        return loginTry;
    }

    private void invalidUserLoginWindow(String s) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(s);
            alert.showAndWait();
        });
    }

    void loginAction() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/client.fxml"));
                Parent content = loader.load();
                Scene scene = new Scene(content);
                Stage currentStage = (Stage) stage.getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Okno czatu");
                currentStage.getIcons().add(new Image("icon.png"));
                currentStage.show();
            } catch (Exception e) {
                exitApp();
                e.printStackTrace();
            }

        });
    }
}

