package io.github.razenbaun.jtsml.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Генератор признаков для временного ряда.
 * Поддерживает:
 * - лаги (1,2,3,...)
 * - скользящее среднее
 * - временной индекс
 */
public class FeatureEngineer {

    /**
     * Создаёт матрицу признаков на основе исходного ряда.
     * @param series исходный ряд (значения)
     * @param lags список лагов для включения (например, [1,2,3])
     * @param rollingWindow окно для скользящего среднего (0 – не использовать)
     * @param includeTimeIndex добавить индекс времени как признак
     * @return FeatureSet, где каждая строка – наблюдение, признаки – лаги, скользящее среднее, индекс
     */
    public static FeatureSet createFeatures(List<Double> series,
                                            List<Integer> lags,
                                            int rollingWindow,
                                            boolean includeTimeIndex) {
        int n = series.size();
        // Определяем количество признаков
        int numFeatures = lags.size() + (rollingWindow > 0 ? 1 : 0) + (includeTimeIndex ? 1 : 0);
        List<double[]> featureRows = new ArrayList<>();
        List<Double> targetRows = new ArrayList<>();

        // Для каждого индекса, начиная с максимального лага (чтобы были все лаги)
        int startIdx = lags.stream().max(Integer::compare).orElse(0);
        if (rollingWindow > 0 && rollingWindow > startIdx) startIdx = rollingWindow;
        if (startIdx == 0) startIdx = 1; // хотя бы для индекса времени

        for (int t = startIdx; t < n; t++) {
            double[] row = new double[numFeatures];
            int col = 0;

            // Лаги
            for (int lag : lags) {
                row[col++] = series.get(t - lag);
            }

            // Скользящее среднее
            if (rollingWindow > 0) {
                double sum = 0;
                for (int i = t - rollingWindow; i < t; i++) {
                    sum += series.get(i);
                }
                row[col++] = sum / rollingWindow;
            }

            // Индекс времени
            if (includeTimeIndex) {
                row[col++] = t;
            }

            featureRows.add(row);
            targetRows.add(series.get(t));
        }

        double[][] features = featureRows.toArray(new double[0][]);
        double[] targets = targetRows.stream().mapToDouble(Double::doubleValue).toArray();
        return new FeatureSet(features, targets);
    }
}
