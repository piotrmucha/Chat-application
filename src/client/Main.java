package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.scene.image.Image;

public class Main extends Application {
    static LogController myControllerHandle;
    static AnchorPane rootPane;
    static Scene scene;
    final static int ServerPort = 4999;
    static Stage primStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/login.fxml"));
        primStage=primaryStage;
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primStage.setTitle("Okno logowania");
        primStage.getIcons().add(new Image("resources/icon.png"));
        primStage.setScene(scene);
        primStage.show();
    }


    public static void main(String[] args) {
        launch(args);


    }
/*    public static void openWeb(String url)  {
        try {
            Desktop.getDesktop().browse(new URL("https://google.com").toURI());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }*/
}
