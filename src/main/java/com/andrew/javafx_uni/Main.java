package com.andrew.javafx_uni;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oshi.SystemInfo;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader root =  new FXMLLoader(getClass().getResource("view.fxml"));

        Scene scene = new Scene(root.load());
        stage.setResizable(true);
        stage.setTitle("Laba 3");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}