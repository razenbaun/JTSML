package io.github.razenbaun.jtsml.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TimeSeriesDataLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadFromCSVWithCommaDelimiter() throws IOException {
        Path file = tempDir.resolve("data.csv");
        String content = "Date,Value\n2021-01-01,100.5\n2021-01-02,200.3\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);
        List<Double> values = TimeSeriesDataLoader.loadFromCSV(file.toString(), 1);
        assertEquals(2, values.size());
        assertEquals(100.5, values.get(0));
        assertEquals(200.3, values.get(1));
    }

    @Test
    void testLoadFromCSVWithSemicolonAndCommaDecimal() throws IOException {
        Path file = tempDir.resolve("data.csv");
        String content = "Date;Value\n2021-01-01;100,5\n2021-01-02;200,3\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);
        List<Double> values = TimeSeriesDataLoader.loadFromCSV(file.toString(), 1);
        assertEquals(2, values.size());
        assertEquals(100.5, values.get(0));
        assertEquals(200.3, values.get(1));
    }

    @Test
    void testLoadWithBOM() throws IOException {
        Path file = tempDir.resolve("data.csv");
        String content = "\uFEFFDate,Value\n2021-01-01,10.0\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);
        List<Double> values = TimeSeriesDataLoader.loadFromCSV(file.toString(), 1);
        assertEquals(1, values.size());
        assertEquals(10.0, values.get(0));
    }

    @Test
    void testDefaultColumnIndex() throws IOException {
        Path file = tempDir.resolve("data.csv");
        String content = "ignore,value\nabc,123.45\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);
        List<Double> values = TimeSeriesDataLoader.loadFromCSV(file.toString());
        assertEquals(1, values.size());
        assertEquals(123.45, values.get(0));
    }
}
