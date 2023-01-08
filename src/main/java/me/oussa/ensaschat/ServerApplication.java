package me.oussa.ensaschat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ServerApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("Main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Chat Server");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> {
            // controller.kickAll();    // TODO: Restructure the code to make this work (see client structure)
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}