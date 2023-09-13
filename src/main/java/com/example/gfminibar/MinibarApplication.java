package com.example.gfminibar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MinibarApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MinibarApplication.class.getResource("minibar.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage.setTitle("Grammatical Framework Minibar");
        stage.setResizable(false);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());


        stage.setScene(scene);
        stage.show();

        // Get the controller instance
        MinibarController controller = fxmlLoader.getController();

        // Call the populateWordsPane() method to initialize it
        controller.populateWordsPane();
    }

    public static void main(String[] args) {
        launch();
    }
}