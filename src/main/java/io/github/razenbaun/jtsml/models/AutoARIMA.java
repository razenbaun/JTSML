package io.github.razenbaun.jtsml.models;

import java.util.*;

public class AutoARIMA {
    public static ArimaModel findBest(List<Double> data, int maxP, int maxD, int maxQ) {
        double bestAICc = Double.POSITIVE_INFINITY;
        ArimaModel bestModel = null;
        for (int d = 0; d <= maxD; d++) {
            for (int p = 0; p <= maxP; p++) {
                for (int q = 0; q <= maxQ; q++) {
                    if (p == 0 && q == 0) continue;
                    try {
                        ArimaModel model = new ArimaModel(p, d, q);
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
        if (bestModel == null) {
            return new ArimaModel(1, 0, 0); // fallback
        }
        return bestModel;
    }
}
