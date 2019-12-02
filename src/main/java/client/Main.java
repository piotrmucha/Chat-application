package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    static Stage primStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        primStage=primaryStage;
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primStage.setTitle("Okno logowania");
        primStage.getIcons().add(new Image("icon.png"));
        primStage.setScene(scene);
        primStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
