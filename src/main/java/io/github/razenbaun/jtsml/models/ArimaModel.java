package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.Serializable;
import java.util.*;

public class ArimaModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 2L;
    private int p, d, q;
    private double[] arCoeffs;
    private double[] maCoeffs;
    private double intercept;
    private double sigma2;
    private LinkedList<Double> lastErrors;
    private List<Double> lastDiffObs;
    private List<Double> originalTail;
    private int nobs;
    private boolean fitted = false;

    public ArimaModel() { this(0, 1, 1); }

    public ArimaModel(int p, int d, int q) {
        this.p = p; this.d = d; this.q = q;
    }

    @Override
    public void fit(List<Double> trainData) {
        fitted = true;
        List<Double> diff = new ArrayList<>(trainData);
        for (int i = 0; i < d; i++) {
            List<Double> tmp = new ArrayList<>();
            for (int j = 1; j < diff.size(); j++)
                tmp.add(diff.get(j) - diff.get(j-1));
            diff = tmp;
        }
        double[] y = diff.stream().mapToDouble(Double::doubleValue).toArray();
        ARMAXOptimizer optimizer = new ARMAXOptimizer(y, new double[y.length][0], p, q, 200_000);
        optimizer.optimize();
        arCoeffs = optimizer.getArCoeffs();
        maCoeffs = optimizer.getMaCoeffs();
        intercept = optimizer.getIntercept();
        sigma2 = optimizer.getSigma2();
        lastErrors = optimizer.getLastErrors();
        int tailSize = Math.max(Math.max(p, q), 1);
        lastDiffObs = new ArrayList<>(diff.subList(diff.size() - tailSize, diff.size()));
        // Хвост исходного ряда нужен только при d > 0
        originalTail = (d > 0) ? new ArrayList<>(trainData.subList(trainData.size() - d, trainData.size())) : new ArrayList<>();
        nobs = diff.size();
    }

    @Override
    public List<Double> predict(int horizon) {
        if (!fitted) throw new IllegalStateException("Model not fitted");
        List<Double> diffForecast = new ArrayList<>();
        LinkedList<Double> workDiff = new LinkedList<>(lastDiffObs);
        LinkedList<Double> workErrors = new LinkedList<>(lastErrors);
        for (int h = 0; h < horizon; h++) {
            double pred = intercept;
            for (int i = 0; i < p; i++) {
                int idx = workDiff.size() - 1 - i;
                if (idx >= 0) pred += arCoeffs[i] * workDiff.get(idx);
            }
            for (int i = 0; i < q; i++) {
                int lag = i + 1;
                if (workErrors.size() >= lag) pred += maCoeffs[i] * workErrors.get(workErrors.size() - lag);
            }
            diffForecast.add(pred);
            workDiff.add(pred);
            workErrors.add(0.0);
        }
        // Если d == 0, разностей нет, прогноз сразу в уровнях
        if (d == 0) {
            return diffForecast;
        }
        // Интегрируем обратно
        List<Double> result = new ArrayList<>();
        List<Double> orig = new ArrayList<>(originalTail);
        for (double dVal : diffForecast) {
            double last = orig.get(orig.size() - 1);
            double next = last + dVal;
            orig.add(next);
            result.add(next);
        }
        return result;
    }

    // AICc: AIC + 2k(k+1)/(n-k-1), n = nobs, k = количество параметров
    public double getAICc() {
        int k = p + q + 1 + (d > 0 ? 1 : 0); // d считается одним параметром (порядок)
        if (nobs - k - 1 <= 0) return Double.POSITIVE_INFINITY;
        double aic = nobs * Math.log(sigma2) + 2 * k;
        return aic + (2.0 * k * (k + 1)) / (nobs - k - 1.0);
    }

    public double getSigma2() { return sigma2; }
    @Override
    public String getName() { return "ARIMA(" + p + "," + d + "," + q + ")"; }
}
