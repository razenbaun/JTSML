package io.github.razenbaun.jtsml.models;

import io.github.razenbaun.jtsml.core.TimeSeriesModelWithExog;
import org.apache.commons.math3.linear.*;
import java.io.Serializable;
import java.util.*;

public class ARIMAXModel implements TimeSeriesModelWithExog, Serializable {
    private static final long serialVersionUID = 2L;
    private int p, d, q;
    private List<Integer> exogLags;
    private double[] arCoeffs;
    private double[] maCoeffs;
    private double[] exogCoeffs;
    private double intercept;
    private double sigma2;
    private LinkedList<Double> lastErrors;
    private List<Double> lastDiffObs;
    private List<Double> originalTail;

    public ARIMAXModel(int p, int d, int q, List<Integer> exogLags) {
        this.p = p; this.d = d; this.q = q;
        this.exogLags = exogLags;
    }

    @Override
    public void fit(List<Double> trainData) {
        fit(trainData, Collections.emptyList());
    }

    @Override
    public void fit(List<Double> target, List<List<Double>> exog) {
        if (exog.size() != exogLags.size())
            throw new IllegalArgumentException("Exog count must match exogLags");
        List<Double> diff = new ArrayList<>(target);
        for (int i = 0; i < d; i++) {
            List<Double> tmp = new ArrayList<>();
            for (int j = 1; j < diff.size(); j++)
                tmp.add(diff.get(j) - diff.get(j-1));
            diff = tmp;
        }
        int maxLag = exogLags.stream().mapToInt(Integer::intValue).max().orElse(0);
        int nobs = diff.size() - maxLag;
        if (nobs < 10) {
            throw new RuntimeException("Not enough observations after lags (need at least 10, got " + nobs + ")");
        }

        if (nobs <= 0) throw new RuntimeException("Not enough observations");

        double[] y = new double[nobs];
        double[][] exogMatrix = new double[nobs][exogLags.size()];
        for (int t = 0; t < nobs; t++) {
            int idx = t + maxLag;
            y[t] = diff.get(idx);
            for (int i = 0; i < exogLags.size(); i++) {
                exogMatrix[t][i] = exog.get(i).get(idx - exogLags.get(i));
            }
        }
        ARMAXOptimizer optimizer = new ARMAXOptimizer(y, exogMatrix, p, q, 200_000);
        optimizer.optimize();
        this.arCoeffs = optimizer.getArCoeffs();
        this.maCoeffs = optimizer.getMaCoeffs();
        this.exogCoeffs = optimizer.getExogCoeffs();
        this.intercept = optimizer.getIntercept();
        this.sigma2 = optimizer.getSigma2();
        this.lastErrors = optimizer.getLastErrors();
        lastDiffObs = new ArrayList<>(diff.subList(diff.size() - Math.max(p, q), diff.size()));
        originalTail = new ArrayList<>(target.subList(target.size() - d, target.size()));
    }

    @Override
    public List<Double> predict(int horizon) {
        return predict(horizon, Collections.emptyList());
    }

    @Override
    public List<Double> predict(int horizon, List<List<Double>> futureExog) {
        List<Double> diffForecast = new ArrayList<>();
        LinkedList<Double> workDiff = new LinkedList<>(lastDiffObs);
        LinkedList<Double> workErrors = new LinkedList<>(lastErrors);
        for (int h = 0; h < horizon; h++) {
            double pred = intercept;
            for (int i = 0; i < p; i++)
                pred += arCoeffs[i] * workDiff.get(workDiff.size() - 1 - i);
            for (int i = 0; i < q; i++) {
                int lag = i + 1;
                if (workErrors.size() > lag)
                    pred += maCoeffs[i] * workErrors.get(workErrors.size() - 1 - lag);
            }
            if (!futureExog.isEmpty()) {
                for (int i = 0; i < exogLags.size(); i++)
                    pred += exogCoeffs[i] * futureExog.get(i).get(h);
            }
            diffForecast.add(pred);
            workDiff.add(pred);
            workErrors.add(0.0);
        }
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

    @Override
    public String getName() {
        return "ARIMAX(" + p + "," + d + "," + q + ")";
    }
}
