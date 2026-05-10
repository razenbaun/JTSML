package io.github.razenbaun.jtsml.export;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;

import java.io.*;

public class ModelExporter {
    public static void saveModel(TimeSeriesModel model, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(model);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends TimeSeriesModel> T loadModel(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (T) ois.readObject();
        }
    }

    public static String exportToJson(TimeSeriesModel model) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"name\":\"").append(model.getName()).append("\"");
        sb.append("}");
        return sb.toString();
    }
}