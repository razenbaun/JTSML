package io.github.razenbaun.jtsml.core;

import java.io.Serializable;
import java.util.List;

public interface TimeSeriesModelWithExog extends TimeSeriesModel, Serializable {
    // Обучение с экзогенными переменными (список рядов одинаковой длины)
    void fit(List<Double> target, List<List<Double>> exog);

    // Прогноз с будущими значениями экзогенных переменных
    List<Double> predict(int horizon, List<List<Double>> futureExog);
}