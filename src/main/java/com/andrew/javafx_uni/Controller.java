package com.andrew.javafx_uni;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    public long time = 0;

    private final SystemInfo systemInfo = new SystemInfo();

    private final CentralProcessor cpu = systemInfo.getHardware().getProcessor();

    private final GlobalMemory ram = systemInfo.getHardware().getMemory();

    public Timer timer = new Timer();

    @FXML
    private MenuItem infoOS, infoCPU, loadCPU, loadRAM, showProc;

    @FXML
    private void initialize() {
        infoOS.setOnAction(e -> showInfoOS());

        infoCPU.setOnAction(e -> showInfoCPU());

        loadCPU.setOnAction(e -> openGraphCPU());

        loadRAM.setOnAction(e -> openGraphRAM());

        showProc.setOnAction(e -> showProcTable());
    }

    private void showInfoOS() {
        Alert osAlert = new Alert(Alert.AlertType.INFORMATION);
        osAlert.setTitle("OS Info");
        osAlert.setHeaderText(null);
        osAlert.setContentText("Operating System: " + systemInfo.getOperatingSystem());
        osAlert.showAndWait();
    }

    private void showInfoCPU() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CPU Information");
        alert.setHeaderText(null);
        alert.setContentText("CPU Name: " + cpu.getProcessorIdentifier().getName() + "\n" +
                "CPU Model: " + cpu.getProcessorIdentifier().getModel() + "\n" +
                "Number of Cores: " + cpu.getLogicalProcessorCount());
        alert.showAndWait();
    }

    private void openGraphCPU() {
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
        Thread updateThread = getCPULoadThread(series);
        updateThread.start();

        lineChart.getData().add(series);

        secondStage.setScene(scene);
        secondStage.setTitle("CPU Load Graph");
        secondStage.show();
    }

    private Thread getCPULoadThread(XYChart.Series<Number, Number> series) {
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
        return updateThread;
    }

    private void openGraphRAM() {
        Stage secondStage = new Stage();

        long totalRAM = ram.getTotal();
        long availRAM = ram.getAvailable();
        long usedRAM = totalRAM - availRAM;

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("RAM Load (%)");

        xAxis.setTickUnit(1);
        xAxis.setLowerBound(0);
        xAxis.setForceZeroInRange(false);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(byteToGig(totalRAM));

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("RAM Load Graph");

        // Create series for the chart
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("RAM Load");

        Scene scene = new Scene(lineChart, 600, 400);

        Thread updateThread = getRAMLoadThread(usedRAM, series);
        updateThread.start();

        lineChart.getData().add(series);

        secondStage.setScene(scene);
        secondStage.setTitle("RAM Load Graph");
        secondStage.show();
    }

    private Thread getRAMLoadThread(long usedRAM, XYChart.Series<Number, Number> series) {
        Thread updateThread = new Thread(() -> {
            while (true) {
                double ramLoad = (byteToGig(usedRAM));
                Platform.runLater(() -> {
                    series.getData().add(new XYChart.Data<>(time, ramLoad));
                    time++;
                });
                try {
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        updateThread.setDaemon(true);
        return updateThread;
    }

    private double byteToGig(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

    private ObservableList<ProcessInfo> data;
    private final OperatingSystem os = systemInfo.getOperatingSystem();

    private void showProcTable() {
        Stage procStage = new Stage();

        Menu procMenu = new Menu("Processes");
        MenuItem stopRef = new MenuItem("Stop");
        MenuItem refresh = new MenuItem("Refresh");
        MenuItem start = new MenuItem("Start");

        start.setOnAction(e -> startUpdate());
        refresh.setOnAction(e -> updateTable());
        stopRef.setOnAction(e -> stopUpdate());

        procMenu.getItems().addAll(start, refresh, stopRef);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(procMenu);

        TableView<ProcessInfo> table = new TableView<>();

        data = FXCollections.observableArrayList();

        table.setItems(data);

        TableColumn<ProcessInfo, String> nameCol = new TableColumn<>("Name");
        TableColumn<ProcessInfo, String> pidCol = new TableColumn<>("PID");

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        pidCol.setCellValueFactory(new PropertyValueFactory<>("pid"));

        table.getColumns().addAll(nameCol, pidCol);

        BorderPane root = new BorderPane();

        root.setTop(menuBar);
        root.setCenter(table);

        Scene scene = new Scene(root, 400, 500);

        procStage.setScene(scene);
        procStage.setTitle("Processes Table");
        procStage.show();

    }

    private void startUpdate() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTable();
            }
        }, 0, 1000);
    }

    private void stopUpdate() {
        timer.cancel();
    }

    private void updateTable() {
        Platform.runLater(() -> {
            List<OSProcess> processes = os.getProcesses();
            data.clear();
            for (OSProcess process : processes) {
                data.add(new ProcessInfo(process.getName(), process.getProcessID()));
            }
        });
    }

    public static class ProcessInfo {
        private final String name;
        private final int pid;

        public ProcessInfo(String name, int pid) {
            this.name = name;
            this.pid = pid;
        }

        public String getName() {
            return name;
        }

        public int getPid() {
            return pid;
        }
    }
}
