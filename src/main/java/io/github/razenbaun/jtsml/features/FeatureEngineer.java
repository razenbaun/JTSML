package io.github.razenbaun.jtsml.features;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature generator for time series.
 * Supports:
 * - lags (1,2,3,...)
 * - rolling mean
 * - time index
 */
public class FeatureEngineer {

    /**
     * Creates a feature matrix from the original series.
     * @param series the original time series values
     * @param lags list of lags to include (e.g., [1,2,3])
     * @param rollingWindow window size for rolling mean (0 = do not use)
     * @param includeTimeIndex whether to add time index as a feature
     * @return FeatureSet where each row is an observation, features are lags, rolling mean, and time index
     */
    public static FeatureSet createFeatures(List<Double> series,
                                            List<Integer> lags,
                                            int rollingWindow,
                                            boolean includeTimeIndex) {
        int n = series.size();
        // Determine number of features
        int numFeatures = lags.size() + (rollingWindow > 0 ? 1 : 0) + (includeTimeIndex ? 1 : 0);
        List<double[]> featureRows = new ArrayList<>();
        List<Double> targetRows = new ArrayList<>();

        // Start from the maximum lag to ensure all lag values are available
        int startIdx = lags.stream().max(Integer::compare).orElse(0);
        if (rollingWindow > 0 && rollingWindow > startIdx) startIdx = rollingWindow;
        if (startIdx == 0) startIdx = 1; // at least for time index

        for (int t = startIdx; t < n; t++) {
            double[] row = new double[numFeatures];
            int col = 0;

            // Lags
            for (int lag : lags) {
                row[col++] = series.get(t - lag);
            }

            // Rolling mean
            if (rollingWindow > 0) {
                double sum = 0;
                for (int i = t - rollingWindow; i < t; i++) {
                    sum += series.get(i);
                }
                row[col++] = sum / rollingWindow;
            }

            // Time index
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
