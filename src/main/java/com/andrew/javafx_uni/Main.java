package com.andrew.javafx_uni;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SystemInfo sysInfo = new SystemInfo();

        Label OSlabel = new Label("OS: " + sysInfo.getOperatingSystem());
        Label RAMlabel = new Label("RAM: " + sysInfo.getHardware().getMemory().getTotal());
        Label CPUlable = new Label("CPU: " + sysInfo.getHardware().getProcessor());

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