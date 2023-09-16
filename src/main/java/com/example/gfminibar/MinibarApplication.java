package com.example.gfminibar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
//import org.grammaticalframework.pgf.*;

import java.io.IOException;
//import java.util.List;

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
        //MinibarController controller = fxmlLoader.getController();

        // Call the populateWordsPane() method to initialize it
        //controller.populateWordsPane();

        //String gfilename = "C:\\Users\\yusuf_budullah\\IdeaProjects\\GFMinibar\\src\\main\\resources\\Letter (1).pgf";
        //PGF pgf = PGF.readPGF(gfilename);

        //List<String> categories = pgf.getCategories();
        //System.out.println(categories);

    }

    public static void main(String[] args) {
        launch();
    }
}