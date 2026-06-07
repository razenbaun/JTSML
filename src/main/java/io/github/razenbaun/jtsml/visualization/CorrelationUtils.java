package io.github.razenbaun.jtsml.visualization;

import java.util.ArrayList;
import java.util.List;

public class CorrelationUtils {

    /**
     * Computes autocorrelation (ACF) for the given series.
     * @param data   time series
     * @param maxLag maximum lag
     * @return array of ACF values of length maxLag+1 (index 0 = 1.0)
     */
    public static double[] acf(List<Double> data, int maxLag) {
        int n = data.size();
        double mean = data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double[] result = new double[maxLag + 1];
        result[0] = 1.0;

        // compute variance (denominator for ACF)
        double variance = 0.0;
        for (double v : data) variance += (v - mean) * (v - mean);

        for (int lag = 1; lag <= maxLag; lag++) {
            double cov = 0.0;
            for (int t = lag; t < n; t++) {
                cov += (data.get(t) - mean) * (data.get(t - lag) - mean);
            }
            result[lag] = cov / variance;
        }
        return result;
    }

    /**
     * Computes partial autocorrelation (PACF) using Durbin-Levinson recursion.
     * @param data   time series
     * @param maxLag maximum lag
     * @return array of PACF values of length maxLag+1 (index 0 = 1.0)
     */
    public static double[] pacf(List<Double> data, int maxLag) {
        int n = data.size();
        double[] acf = acf(data, maxLag);
        double[] pacf = new double[maxLag + 1];
        pacf[0] = 1.0;
        pacf[1] = acf[1];

        double[] phi = new double[maxLag + 1];
        phi[1] = acf[1];

        for (int k = 2; k <= maxLag; k++) {
            double numerator = acf[k];
            for (int j = 1; j < k; j++) {
                numerator -= phi[j] * acf[k - j];
            }
            double denominator = 1.0;
            for (int j = 1; j < k; j++) {
                denominator -= phi[j] * acf[j];
            }
            phi[k] = numerator / denominator;

            // update phi[1..k-1]
            double[] phiOld = new double[k];
            System.arraycopy(phi, 0, phiOld, 0, k);
            for (int j = 1; j < k; j++) {
                phi[j] = phiOld[j] - phi[k] * phiOld[k - j];
            }

            pacf[k] = phi[k];
        }
        return pacf;
    }
}
