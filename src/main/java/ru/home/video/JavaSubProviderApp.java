package ru.home.video;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.home.video.controller.MainController;

import java.io.IOException;


public class JavaSubProviderApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Loading FXML file for russian language by default
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Parent root = loader.load();

        // 2. gets controller and give him stage
        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        // 3. sets up stage
        Scene scene = new Scene(root, 625, 500);
        primaryStage.setTitle("🎬 JavaSubProvider");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(625);
        primaryStage.setMinHeight(500);

        // 4. show the window
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args); // prepare a user interface of the desktop app
    }
}
