package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holt's model (double exponential smoothing) – accounts for trend.
 * Parameters: alpha – level smoothing, beta – trend smoothing.
 */
public class HoltModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private double alpha;
    private double beta;
    private double lastLevel;
    private double lastTrend;
    private String name = "Holt";

    public HoltModel(double alpha, double beta) {
        if (alpha <= 0 || alpha >= 2) throw new IllegalArgumentException("Alpha must be in (0,2)");
        if (beta <= 0 || beta >= 2) throw new IllegalArgumentException("Beta must be in (0,2)");
        this.alpha = alpha;
        this.beta = beta;
    }

    public HoltModel() { this(0.3, 0.1); }

    @Override
    public void fit(List<Double> trainData) {
        if (trainData == null || trainData.size() < 2)
            throw new IllegalArgumentException("Need at least 2 observations");
        lastLevel = trainData.get(0);
        lastTrend = trainData.get(1) - trainData.get(0);
        for (int t = 1; t < trainData.size(); t++) {
            double newLevel = alpha * trainData.get(t) + (1 - alpha) * (lastLevel + lastTrend);
            lastTrend = beta * (newLevel - lastLevel) + (1 - beta) * lastTrend;
            lastLevel = newLevel;
        }
    }

    @Override
    public List<Double> predict(int horizon) {
        List<Double> forecast = new ArrayList<>();
        for (int i = 1; i <= horizon; i++) {
            forecast.add(lastLevel + lastTrend * i);
        }
        return forecast;
    }

    @Override
    public String getName() { return name + "(α=" + alpha + ", β=" + beta + ")"; }
}
