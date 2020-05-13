package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static messages.KindOfMessage.*;

public class ClientController {

    private static String MUSIC_PATH = "notificationVoice.wav";
    private static String LINK_MESSAGE_STYLE = "-fx-text-fill: #0066CC";
    private static String REGEX_PATTERN = "https?:\\/\\/(www\\.)?[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ0-9@:%_\\+.~#?&//=]*)";

    private static Logger LOGGER = LogManager.getLogger(ClientController.class);

    private int counterValue;
    private String nick;
    private Socket socket;
    private Stage thisStage;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    @FXML
    private Text counterTextField;
    @FXML
    private ScrollPane scroll;
    @FXML
    private TextFlow outputArea;
    @FXML
    private TextArea messagesArea;

    public ClientController() {
        thisStage = Main.primStage;
        sInput = LogController.input;
        socket = LogController.socket;
        nick = LogController.userName;
        sOutput = LogController.output;
        counterValue = LogController.userCounts;
    }

    public void initialize() {
        scroll.vvalueProperty().bind(outputArea.heightProperty());
        counterTextField.setText(Integer.toString(counterValue));
        setupMessageArea();
        setupServerListener();
        setupOnCloseRequest();
    }

    private void setupOnCloseRequest() {
        thisStage.setOnCloseRequest(e -> {
            Message exitMessage = new Message();
            exitMessage.setKindOfMessage(DISCONNECTION);
            tryToWriteMessage(exitMessage);
            disconnect();
            Platform.exit();
            System.exit(0);
        });
    }

    private void tryToWriteMessage(Message exitMessage) {
        try {
            sOutput.writeObject(exitMessage);
        } catch (Exception ex) {
            exitApp();
            ex.printStackTrace();
        }
    }

    private void setupServerListener() {
        Thread serverListener = new ListenFromServer();
        serverListener.setDaemon(true);
        serverListener.start();
    }

    private void setupMessageArea() {
        messagesArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (!messagesArea.getText().trim().isEmpty()) {
                    sendMessage();
                }
            }
        });
    }

    private void exitApp() {
        Platform.runLater(() -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void disconnect() {
        try {
            sInput.close();
            sOutput.close();
            socket.close();
        } catch (Exception e) {
            exitApp();
            e.printStackTrace();
        }
    }

    @FXML
    void emoticon01Fun() {
        messagesArea.appendText("😀");
    }

    @FXML
    void emoticon02Fun() {
        messagesArea.appendText("😘🤣🤣🙄");
    }

    @FXML
    void emoticon03Fun() {
        messagesArea.appendText("😶😬👺🙄🙄");
    }

    @FXML
    void emoticon04Fun() {
        messagesArea.appendText("😡");
    }

    @FXML
    void emoticon05Fun() {
        messagesArea.appendText("\uD83D\uDE14");
    }

    @FXML
    void emoticon06Fun() {
        messagesArea.appendText("😇");
    }

    @FXML
    void emoticon07Fun() {
        messagesArea.appendText("🙀");
    }

    @FXML
    void emoticon08Fun() {
        messagesArea.appendText("🤣(｡◕‿◕｡)");
    }

    @FXML
    void sendMessage() {
        String received = messagesArea.getText();
        int receivedMsgLength = received.length();
        if (validateTextLength(received, receivedMsgLength)) {
            messagesArea.setText("");
            Message toSent = createMessageToSend(received);
            tryToWriteMessage(toSent);
        } else if (receivedMsgLength >= 280) {
            displayAlterWindow("Nie udało się wysład wiadomości.\n Przekroczono limit znaków (280)");
        }
    }

    private void displayAlterWindow(String s) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(s);
            alert.showAndWait();
        });
    }

    private Message createMessageToSend(String received) {
        String encrypted = encrypt(received);
        Message toSent = new Message();
        toSent.setUserName(nick);
        toSent.setKindOfMessage(STANDARD_MESSAGE);
        toSent.setContent(encrypted);
        checkLink(received, nick, false);
        return toSent;
    }

    private boolean validateTextLength(String received, int len) {
        return !received.isEmpty() && len < 280;
    }

    private void playMusic() {
        try {
            URL res = getClass().getClassLoader().getResource(MUSIC_PATH);
            File file = Paths.get(res.toURI()).toFile();
            String path = file.getAbsolutePath();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | URISyntaxException exc) {
            LOGGER.error("exception while try to play notification sign after message received, error: {}", exc.getLocalizedMessage());
            exitApp();
            exc.printStackTrace();
        }
    }

    private String encrypt(String text) {
        StringBuilder sb = new StringBuilder(text);
        int distance = 7;
        for (int i = 0; i < sb.length(); i++) {
            int c = sb.charAt(i);
            if (c > 31 && c < 123) {
                if (c + distance > 122) {
                    c = 31 + (distance - (122 - c));
                } else {
                    c += distance;
                }
                sb.setCharAt(i, (char) c);
            }
        }
        return sb.toString();
    }

    private String decrypt(String text) {
        StringBuilder sb = new StringBuilder(text);
        int distance = 7;
        for (int i = 0; i < sb.length(); i++) {
            int c = sb.charAt(i);
            if (c > 31 && c < 123) {
                if (c - distance < 32) {
                    c = 123 - (32 - (c - distance));
                } else {
                    c -= distance;
                }
                sb.setCharAt(i, (char) c);
            }
        }
        return sb.toString();
    }

    private void checkLink(String msg, String userName, boolean play) {
        Text nickArea = prepareNickArea(userName);
        Boolean[] array = new Boolean[msg.length()];
        ArrayList arr = markLinkLetters(msg, array);
        int index = 0;
        while (index < msg.length()) {
            StringBuilder part = new StringBuilder();
            if (array[index]) {
                while (index < msg.length() && array[index]) {
                    part.append(msg.charAt(index));
                    index++;
                }
                Hyperlink link = setupLinkOnClick(part);
                arr.add(link);
            } else {
                while (index < msg.length() && !array[index]) {
                    part.append(msg.charAt(index));
                    index++;
                }
                Text message = new Text(part.toString());
                arr.add(message);
            }
        }
        appendMsgToMainArea(msg, play, nickArea, arr);
    }

    private void appendMsgToMainArea(String msg, boolean play, Text nickArea, ArrayList arr) {
        Platform.runLater(() -> {
            outputArea.getChildren().add(nickArea);
            for (int k = 0; k < arr.size(); k++) {
                Object element = arr.get(k);
                if (element instanceof Text) {
                    outputArea.getChildren().add((Text) element);
                } else {
                    outputArea.getChildren().add((Hyperlink) element);
                }
            }
            if ((msg.trim()).equals(msg)) {//this can check if event is "ButtonEvent" because  "KeyPressedEvent" generate additional new line
                outputArea.getChildren().add(new Text(System.lineSeparator()));
            }
            if (play) playMusic();
        });
    }

    private Hyperlink setupLinkOnClick(StringBuilder part) {
        Hyperlink link = new Hyperlink(part.toString());
        link.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URL(part.toString()).toURI());
            } catch (Exception ex) {
                displayAlterWindow("Nie udało otworzyc hiperlinku. Sprawdź czy masz zainstalowaną przeglądarkę internetową.\n " +
                        "Sprawdź czy jedna z zainstalowanych przeglądarek jest przeglądarką domyślną.");
                ex.printStackTrace();
            }
        });
        link.setStyle(LINK_MESSAGE_STYLE);
        return link;
    }

    private ArrayList markLinkLetters(String msg, Boolean[] array) {
        Arrays.fill(array, 0, msg.length(), false);
        Pattern pattern = Pattern.compile(REGEX_PATTERN);
        Matcher matcher = pattern.matcher(msg);
        ArrayList arr = new ArrayList();
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            Arrays.fill(array, matchStart, matchEnd, true);
        }
        return arr;
    }

    private Text prepareNickArea(String userName) {
        Text nickArea = new Text(userName + ": ");
        nickArea.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        return nickArea;
    }

    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                receiveMessage();
            }
        }

        private void receiveMessage() {
            Message received;
            try {
                received = (Message) sInput.readObject();
                if (received.getKindOfMessage() == STANDARD_MESSAGE) {
                    String msg = received.getContent();
                    msg = decrypt(msg);
                    checkLink(msg, received.getUserName(), true);
                } else if (received.getKindOfMessage() == USER_COUNTER) {
                    counterTextField.setText(Integer.toString(received.getUsersCounter()));
                } else if (received.getKindOfMessage() == DISCONNECTION) {
                    counterTextField.setText(Integer.toString(received.getUsersCounter()));
                }

            } catch (Exception e) {
                closeStreams();
            }
        }

        private void closeStreams() {
            try {
                sOutput.close();
                sInput.close();
                socket.close();
                exitApp();
            } catch (IOException e01) {
                exitApp();
                e01.printStackTrace();
            }
        }
    }
}
