package com.andrew.javafx_uni;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oshi.SystemInfo;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SystemInfo sysInfo = new SystemInfo();

        Label OSlabel = new Label("OS: " + sysInfo.getOperatingSystem());
        Label RAMlabel = new Label("RAM: " + sysInfo.getHardware().getMemory().getTotal());
        Label CPUlable = new Label("CPU: " + sysInfo.getHardware().getProcessor());

        VBox root = new VBox(10);
        root.getChildren().addAll(OSlabel, RAMlabel, CPUlable);

        Scene scene = new Scene(root, 320, 240);
        stage.setTitle("Laba 2");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}