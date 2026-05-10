package io.github.razenbaun.jtsml.adapters;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProphetAdapterWithDates implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private final String pythonPath;
    private final String scriptPath;
    private List<Double> trainData;
    private List<String> trainDates;   // "yyyy-MM-dd"
    private boolean fitted = false;

    public ProphetAdapterWithDates(String pythonPath, String scriptPath) {
        this.pythonPath = pythonPath;
        this.scriptPath = scriptPath;
    }

    @Override
    public void fit(List<Double> trainData) {
        throw new UnsupportedOperationException("Use fit(values, dates) instead");
    }

    public void fit(List<Double> values, List<String> dates) {
        this.trainData = new ArrayList<>(values);
        this.trainDates = new ArrayList<>(dates);
        this.fitted = true;
    }

    @Override
    public List<Double> predict(int horizon) {
        if (!fitted) throw new IllegalStateException("Model not fitted");
        try {
            Path trainFile = Files.createTempFile("prophet_train_", ".csv");
            Path forecastFile = Files.createTempFile("prophet_forecast_", ".csv");

            // Записываем CSV с реальными датами
            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(trainFile))) {
                pw.println("ds,y");
                for (int i = 0; i < trainData.size(); i++) {
                    pw.printf("%s,%.6f%n", trainDates.get(i), trainData.get(i));
                }
            }

            List<String> command = new ArrayList<>();
            command.add(pythonPath);
            command.add(scriptPath);
            command.add(trainFile.toAbsolutePath().toString());
            command.add(forecastFile.toAbsolutePath().toString());
            command.add(String.valueOf(horizon));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("ProphetWithDates script failed: " + output);
            }

            List<Double> forecast = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(forecastFile)) {
                br.readLine(); // заголовок
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        forecast.add(Double.parseDouble(parts[1]));
                    }
                }
            }

            Files.deleteIfExists(trainFile);
            Files.deleteIfExists(forecastFile);
            return forecast;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("ProphetWithDates execution error", e);
        }
    }

    @Override
    public String getName() {
        return "ProphetWithDates";
    }
}
