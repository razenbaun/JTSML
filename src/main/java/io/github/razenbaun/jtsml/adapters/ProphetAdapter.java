package io.github.razenbaun.jtsml.adapters;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ProphetAdapter implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private final String pythonPath;
    private final String scriptPath;
    private List<Double> trainData;
    private boolean fitted = false;

    public ProphetAdapter(String pythonPath, String scriptPath) {
        this.pythonPath = pythonPath;
        this.scriptPath = scriptPath;
    }

    @Override
    public void fit(List<Double> trainData) {
        this.trainData = new ArrayList<>(trainData);
        this.fitted = true;
    }

    @Override
    public List<Double> predict(int horizon) {
        if (!fitted) throw new IllegalStateException("Model not fitted");
        try {
            Path trainFile = Files.createTempFile("prophet_train_", ".csv");
            Path forecastFile = Files.createTempFile("prophet_forecast_", ".csv");

            // Write training data using dot as decimal separator (Locale.US)
            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(trainFile))) {
                pw.println("ds,y");
                for (int i = 0; i < trainData.size(); i++) {
                    pw.printf(Locale.US, "%d,%.6f%n", i, trainData.get(i));
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
                throw new RuntimeException("Prophet script failed: " + output);
            }

            List<Double> forecast = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(forecastFile)) {
                br.readLine();
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
            throw new RuntimeException("Prophet execution error", e);
        }
    }

    @Override
    public String getName() {
        return "ProphetAdapter";
    }
}
