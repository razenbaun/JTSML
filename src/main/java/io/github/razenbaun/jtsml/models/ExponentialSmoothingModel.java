package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExponentialSmoothingModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private double alpha;
    private double lastLevel;
    private String name = "ExponentialSmoothing";

    public ExponentialSmoothingModel(double alpha) {
        if (alpha <= 0 || alpha >= 2)
            throw new IllegalArgumentException("Alpha must be in (0,2)");
        this.alpha = alpha;
    }

    public ExponentialSmoothingModel() { this(0.3); }

    @Override
    public void fit(List<Double> trainData) {
        if (trainData == null || trainData.isEmpty())
            throw new IllegalArgumentException("Train data cannot be empty");
        double level = trainData.get(0);
        for (int t = 1; t < trainData.size(); t++)
            level = alpha * trainData.get(t) + (1 - alpha) * level;
        lastLevel = level;
    }

    @Override
    public List<Double> predict(int horizon) {
        List<Double> forecast = new ArrayList<>();
        for (int i = 0; i < horizon; i++) forecast.add(lastLevel);
        return forecast;
    }

    @Override
    public String getName() { return name + "(α=" + alpha + ")"; }
}
