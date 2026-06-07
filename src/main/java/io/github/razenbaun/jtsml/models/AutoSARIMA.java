package io.github.razenbaun.jtsml.models;

import java.util.*;

public class AutoSARIMA {

    /**
     * Finds the best SARIMA model with the given seasonal period.
     * @param data           time series data
     * @param maxP           maximum non‑seasonal AR order
     * @param maxD           maximum non‑seasonal I order
     * @param maxQ           maximum non‑seasonal MA order
     * @param maxSeasonalP   maximum seasonal AR order
     * @param maxSeasonalD   maximum seasonal I order
     * @param maxSeasonalQ   maximum seasonal MA order
     * @param s              seasonal period (7 for daily, 12 for monthly, etc.)
     * @return               the best model, or null if none converged
     */
    public static SARIMAModel findBest(List<Double> data,
                                       int maxP, int maxD, int maxQ,
                                       int maxSeasonalP, int maxSeasonalD, int maxSeasonalQ,
                                       int s) {
        double bestAICc = Double.POSITIVE_INFINITY;
        SARIMAModel bestModel = null;

        for (int d = 0; d <= maxD; d++) {
            for (int p = 0; p <= maxP; p++) {
                for (int q = 0; q <= maxQ; q++) {
                    for (int D = 0; D <= maxSeasonalD; D++) {
                        for (int P = 0; P <= maxSeasonalP; P++) {
                            for (int Q = 0; Q <= maxSeasonalQ; Q++) {
                                if (p == 0 && q == 0 && P == 0 && Q == 0) continue;
                                try {
                                    SARIMAModel model = new SARIMAModel(p, d, q, P, D, Q, s);
                                    model.fit(data);
                                    double aicc = model.getAICc();
                                    if (aicc < bestAICc) {
                                        bestAICc = aicc;
                                        bestModel = model;
                                    }
                                } catch (Exception e) {
                                    // skip
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestModel;
    }
}
