package io.github.razenbaun.jtsml.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TimeSeriesDataLoader {

    /**
     * Loads a time series, automatically detecting encoding.
     * First tries UTF-8, on failure – Windows-1251.
     */
    public static List<Double> loadFromCSV(String filePath, int valueColumnIndex) throws IOException {
        try {
            return loadFromCSV(filePath, valueColumnIndex, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return loadFromCSV(filePath, valueColumnIndex, Charset.forName("windows-1251"));
        }
    }

    public static List<Double> loadFromCSV(String filePath, int valueColumnIndex, Charset charset) throws IOException {
        Path path = Path.of(filePath);
        // Read all bytes and convert to string using the given encoding
        byte[] bytes = Files.readAllBytes(path);
        String content = new String(bytes, charset);

        // Remove BOM (if present)
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        // Determine delimiter
        char delimiter = content.contains(";") ? ';' : ',';

        String[] lines = content.split("\\r?\\n");
        boolean headerSkipped = false;
        List<Double> values = new ArrayList<>();

        for (String line : lines) {
            if (line.isBlank()) continue;

            // Skip lines that do not contain digits (headers)
            if (!headerSkipped && !line.matches(".*\\d+.*")) {
                headerSkipped = true;
                continue;
            }

            String[] parts = line.split(String.valueOf(delimiter));
            if (parts.length <= valueColumnIndex) continue;

            String valueStr = parts[valueColumnIndex].trim().replace(',', '.');
            try {
                values.add(Double.parseDouble(valueStr));
            } catch (NumberFormatException e) {
                System.err.println("Skipping non-numeric: " + valueStr);
            }
        }
        return values;
    }

    public static List<Double> loadFromCSV(String filePath) throws IOException {
        return loadFromCSV(filePath, 1);
    }
}
