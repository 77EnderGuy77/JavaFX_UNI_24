package com.andrew.javafx_uni;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oshi.*;
import oshi.hardware.CentralProcessor;

public class Controller {
    public long time = 0;

    private final SystemInfo systemInfo = new SystemInfo();
    private final CentralProcessor cpu = systemInfo.getHardware().getProcessor();

    @FXML
    private MenuItem infoOS, infoCPU, loadCPU;

    @FXML
    private void initialize(){
        infoOS.setOnAction(e -> showInfoOS());
        infoCPU.setOnAction(e -> showInfoCPU());
        loadCPU.setOnAction(e -> openGraph());
    }

    private void showInfoOS(){
        Alert osAlert = new Alert(Alert.AlertType.INFORMATION);
        osAlert.setTitle("OS Info");
        osAlert.setHeaderText(null);
        osAlert.setContentText("Operating System: " + systemInfo.getOperatingSystem());
        osAlert.showAndWait();
    }

    private void showInfoCPU(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CPU Information");
        alert.setHeaderText(null);
        alert.setContentText("CPU Name: " + cpu.getProcessorIdentifier().getName() + "\n" +
                "CPU Model: " + cpu.getProcessorIdentifier().getModel() + "\n" +
                "Number of Cores: " + cpu.getLogicalProcessorCount());
        alert.showAndWait();
    }

    private void openGraph() {
        Stage secondStage = new Stage();

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("CPU Load (%)");

        xAxis.setTickUnit(1);
        xAxis.setLowerBound(0);
        xAxis.setForceZeroInRange(false);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("CPU Load Graph");

        // Create series for the chart
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("CPU Load");

        StackPane root = new StackPane(lineChart);
        Scene scene = new Scene(root, 600, 400);
        // Update CPU load label periodically
        Thread updateThread = new Thread(() -> {
            long[] prevTicks = cpu.getSystemCpuLoadTicks();

            while (true) {
                double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
                Platform.runLater(() -> {
                    series.getData().add(new XYChart.Data<>(time, cpuLoad));
                    time++;
                });
                prevTicks = cpu.getSystemCpuLoadTicks();
                try {
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();

        lineChart.getData().add(series);

        secondStage.setScene(scene);
        secondStage.setTitle("CPU Load Graph");
        secondStage.show();
    }
}
