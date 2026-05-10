package io.github.razenbaun.jtsml.core;

import java.io.Serializable;
import java.util.List;

public interface TimeSeriesModel extends Serializable {
    void fit(List<Double> trainData);
    List<Double> predict(int horizon);
    String getName();
}