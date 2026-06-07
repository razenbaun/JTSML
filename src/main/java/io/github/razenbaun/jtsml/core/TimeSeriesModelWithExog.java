package io.github.razenbaun.jtsml.core;

import java.io.Serializable;
import java.util.List;

public interface TimeSeriesModelWithExog extends TimeSeriesModel, Serializable {
    void fit(List<Double> target, List<List<Double>> exog);

    List<Double> predict(int horizon, List<List<Double>> futureExog);
}