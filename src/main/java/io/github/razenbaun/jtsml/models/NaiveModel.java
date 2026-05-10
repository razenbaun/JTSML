package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NaiveModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private double lastValue;
    private final String name = "Naïve";

    @Override
    public void fit(List<Double> trainData) {
        if (trainData == null || trainData.isEmpty())
            throw new IllegalArgumentException("Train data cannot be empty");
        lastValue = trainData.get(trainData.size() - 1);
    }

    @Override
    public List<Double> predict(int horizon) {
        List<Double> forecast = new ArrayList<>();
        for (int i = 0; i < horizon; i++) forecast.add(lastValue);
        return forecast;
    }

    @Override
    public String getName() { return name; }
}
