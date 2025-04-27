package com.example.teacherhoursdesktop;

import com.example.teacherhoursdesktop.utils.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Database.initialize();
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("/com/example/teacherhoursdesktop/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Учет часов преподавателей");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}