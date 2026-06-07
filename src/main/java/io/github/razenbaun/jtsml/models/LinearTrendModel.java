package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinearTrendModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private transient SimpleRegression regression; // not serializable
    private double slope;
    private double intercept;
    private double n; // number of points
    private final String name = "LinearTrend";

    @Override
    public void fit(List<Double> trainData) {
        if (trainData == null || trainData.size() < 2)
            throw new IllegalArgumentException("Need at least 2 observations");
        regression = new SimpleRegression();
        for (int t = 0; t < trainData.size(); t++) {
            regression.addData(t, trainData.get(t));
        }
        slope = regression.getSlope();
        intercept = regression.getIntercept();
        n = regression.getN();
    }

    @Override
    public List<Double> predict(int horizon) {
        List<Double> forecast = new ArrayList<>();
        for (int i = 1; i <= horizon; i++) {
            forecast.add(intercept + slope * (n + i - 1));
        }
        return forecast;
    }

    @Override
    public String getName() { return name; }

    // No need to restore regression after deserialization because predict does not use it
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // regression can be skipped – it is not used in predict
    }
}
