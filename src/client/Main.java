package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Main extends Application {
    static LogController myControllerHandle;
    static AnchorPane rootPane;
    static Scene scene;
    final static int ServerPort = 4999;
    static Stage primStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));


        primStage=primaryStage;
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primStage.setTitle("Okno logowania");
        primStage.setScene(scene);
        primStage.show();
    }


    public static void main(String[] args) {
        launch(args);


    }
}
