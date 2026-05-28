package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinearTrendModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private transient SimpleRegression regression; // не сериализуем
    private double slope;
    private double intercept;
    private double n; // количество точек
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

    // Восстановление regression после десериализации не требуется, т.к. predict не использует его
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // regression можно не восстанавливать – он не используется в predict
    }
}
