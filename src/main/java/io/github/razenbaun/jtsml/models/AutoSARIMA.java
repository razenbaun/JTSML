package io.github.razenbaun.jtsml.models;

import java.util.*;

public class AutoSARIMA {

    /**
     * Ищет лучшую SARIMA модель с заданным сезонным периодом.
     * @param data       временной ряд
     * @param maxP       максимальный несезонный AR порядок
     * @param maxD       максимальный несезонный I порядок
     * @param maxQ       максимальный несезонный MA порядок
     * @param maxSeasonalP   максимальный сезонный AR порядок
     * @param maxSeasonalD   максимальный сезонный I порядок
     * @param maxSeasonalQ   максимальный сезонный MA порядок
     * @param s           сезонный период (7 для дней, 12 для месяцев и т.д.)
     * @return           лучшая модель или null, если ни одна не подошла
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
                                    // Пропускаем неудачные комбинации
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
