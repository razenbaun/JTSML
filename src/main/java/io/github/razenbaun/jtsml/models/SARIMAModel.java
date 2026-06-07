package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.Serializable;
import java.util.*;

public class SARIMAModel implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 2L;
    private int p, d, q, P, D, Q, s;
    private double[] arCoeffs;
    private double[] maCoeffs;
    private double intercept;
    private double sigma2;
    private LinkedList<Double> lastErrors;
    private List<Double> lastDiffObs;    // last values of the differenced series
    private List<Double> lastLevels;     // last values of the original series (for integration)
    private int maxArLag;
    private int maxMaLag;
    private int[] arLags;
    private int[] maLags;
    private int nobs;

    public SARIMAModel(int p, int d, int q, int P, int D, int Q, int s) {
        this.p = p; this.d = d; this.q = q;
        this.P = P; this.D = D; this.Q = Q;
        this.s = s;
    }

    @Override
    public void fit(List<Double> trainData) {
        List<Double> transformed = new ArrayList<>(trainData);

        for (int i = 0; i < D; i++) {
            List<Double> tmp = new ArrayList<>();
            for (int j = s; j < transformed.size(); j++) {
                tmp.add(transformed.get(j) - transformed.get(j - s));
            }
            transformed = tmp;
        }
        for (int i = 0; i < d; i++) {
            List<Double> tmp = new ArrayList<>();
            for (int j = 1; j < transformed.size(); j++) {
                tmp.add(transformed.get(j) - transformed.get(j - 1));
            }
            transformed = tmp;
        }

        List<Integer> arLagList = new ArrayList<>();
        for (int i = 1; i <= p; i++) arLagList.add(i);
        for (int i = 1; i <= P; i++) arLagList.add(i * s);
        List<Integer> maLagList = new ArrayList<>();
        for (int i = 1; i <= q; i++) maLagList.add(i);
        for (int i = 1; i <= Q; i++) maLagList.add(i * s);

        arLags = arLagList.stream().mapToInt(i -> i).toArray();
        maLags = maLagList.stream().mapToInt(i -> i).toArray();

        maxArLag = arLags.length > 0 ? Arrays.stream(arLags).max().getAsInt() : 0;
        maxMaLag = maLags.length > 0 ? Arrays.stream(maLags).max().getAsInt() : 0;
        int maxLag = Math.max(maxArLag, maxMaLag);

        double[] y = transformed.stream().mapToDouble(Double::doubleValue).toArray();
        if (y.length - maxLag < 10)
            throw new RuntimeException("Not enough observations after lags");

        ARMAXOptimizer optimizer = new ARMAXOptimizer(y, new double[y.length][0],
                arLags, maLags, 800_000);
        optimizer.optimize();
        arCoeffs = optimizer.getArCoeffs();
        maCoeffs = optimizer.getMaCoeffs();
        intercept = optimizer.getIntercept();
        sigma2 = optimizer.getSigma2();
        lastErrors = optimizer.getLastErrors();

        lastDiffObs = new ArrayList<>(transformed.subList(transformed.size() - maxLag, transformed.size()));

        int needTail = d + s * D;
        lastLevels = new ArrayList<>(trainData.subList(trainData.size() - needTail, trainData.size()));
        nobs = y.length;
    }

    @Override
    public List<Double> predict(int horizon) {
        // Recursive forecast of the differenced series
        LinkedList<Double> workDiff = new LinkedList<>(lastDiffObs);
        LinkedList<Double> workErrors = new LinkedList<>(lastErrors);
        List<Double> diffForecast = new ArrayList<>();

        for (int h = 0; h < horizon; h++) {
            double pred = intercept;
            // AR part
            for (int i = 0; i < arLags.length; i++) {
                int idx = workDiff.size() - arLags[i];
                if (idx >= 0) pred += arCoeffs[i] * workDiff.get(idx);
            }
            // MA part
            for (int i = 0; i < maLags.length; i++) {
                int lag = maLags[i];
                if (workErrors.size() >= lag) {
                    pred += maCoeffs[i] * workErrors.get(workErrors.size() - lag);
                }
            }
            diffForecast.add(pred);
            workDiff.add(pred);
            workErrors.add(0.0); // future errors = 0
        }

        // Integrate the differenced series back to levels
        // Standard sequential integration algorithm
        List<Double> levels = new ArrayList<>(lastLevels);
        for (double dVal : diffForecast) {
            levels.add(dVal); // temporarily add the difference
        }

        // d-fold integration (step 1)
        for (int k = 0; k < d; k++) {
            for (int i = d + s * D; i < levels.size(); i++) {
                double prev = levels.get(i - 1);
                levels.set(i, prev + levels.get(i));
            }
        }

        // D-fold seasonal integration (step s)
        for (int k = 0; k < D; k++) {
            for (int i = d + s * D; i < levels.size(); i++) {
                double prev = levels.get(i - s);
                levels.set(i, prev + levels.get(i));
            }
        }

        return new ArrayList<>(levels.subList(levels.size() - horizon, levels.size()));
    }

    public double getAICc() {
        int k = (p + P) + (q + Q) + 1 + (d > 0 ? 1 : 0) + (D > 0 ? 1 : 0);
        if (nobs - k - 1 <= 0) return Double.POSITIVE_INFINITY;
        double aic = nobs * Math.log(sigma2) + 2 * k;
        return aic + (2.0 * k * (k + 1)) / (nobs - k - 1.0);
    }

    @Override
    public String getName() {
        return "SARIMA(" + p + "," + d + "," + q + ")(" + P + "," + D + "," + Q + ")" + s;
    }
}
