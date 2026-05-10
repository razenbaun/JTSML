package io.github.razenbaun.jtsml.models;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.*;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.*;
import java.util.*;

class ARMAXOptimizer {
    private double[] y;
    private double[][] exog;
    private int[] arLags;
    private int[] maLags;
    private double[] arCoeffs;
    private double[] maCoeffs;
    private double[] exogCoeffs;
    private double intercept;
    private double sigma2;
    private LinkedList<Double> lastErrors;
    private int p, q;
    private int maxEvaluations=400_000;

    ARMAXOptimizer(double[] y, double[][] exog, int p, int q) {
        this(y, exog, p, q, 400_000);
    }

    ARMAXOptimizer(double[] y, double[][] exog, int p, int q, int maxEvaluations) {
        this.y = y;
        this.exog = exog;
        this.p = p;
        this.q = q;
        this.maxEvaluations = maxEvaluations;
        arLags = new int[p];
        for (int i = 0; i < p; i++) arLags[i] = i + 1;
        maLags = new int[q];
        for (int i = 0; i < q; i++) maLags[i] = i + 1;
    }

    ARMAXOptimizer(double[] y, double[][] exog, int[] arLags, int[] maLags, int maxEvaluations) {
        this.y = y;
        this.exog = exog;
        this.arLags = arLags;
        this.maLags = maLags;
        this.p = arLags.length;
        this.q = maLags.length;
        this.maxEvaluations = maxEvaluations;
    }

    void optimize() {
        int n = y.length;
        int numExog = (exog.length > 0 && exog[0] != null) ? exog[0].length : 0;

        int maxArLag = p > 0 ? Arrays.stream(arLags).max().getAsInt() : 0;
        int maxMaLag = q > 0 ? Arrays.stream(maLags).max().getAsInt() : 0;
        int maxLag = Math.max(maxArLag, maxMaLag);

        if (n - maxLag < 10) {
            throw new RuntimeException("Not enough observations after lags");
        }

        // Начальное приближение AR (МНК без MA)
        RealMatrix X = new Array2DRowRealMatrix(n - maxLag, 1 + p + numExog);
        RealVector Yvec = new ArrayRealVector(n - maxLag);
        for (int t = maxLag; t < n; t++) {
            int row = t - maxLag;
            Yvec.setEntry(row, y[t]);
            int col = 0;
            X.setEntry(row, col++, 1.0);
            for (int i = 0; i < p; i++) {
                X.setEntry(row, col++, y[t - arLags[i]]);
            }
            for (int j = 0; j < numExog; j++) {
                X.setEntry(row, col++, exog[t][j]);
            }
        }
        RealVector coeffs = new QRDecomposition(X).getSolver().solve(Yvec);
        double[] initAr = new double[p];
        double[] initExog = new double[numExog];
        double initIntercept = coeffs.getEntry(0);
        for (int i = 0; i < p; i++) initAr[i] = coeffs.getEntry(1 + i);
        for (int j = 0; j < numExog; j++) initExog[j] = coeffs.getEntry(1 + p + j);

        double[] initMa = new double[q];
        Random rand = new Random(42);
        for (int i = 0; i < q; i++) initMa[i] = rand.nextGaussian() * 0.01;

        int paramCount = 1 + p + q + numExog;
        double[] startPoint = new double[paramCount];
        startPoint[0] = initIntercept;
        System.arraycopy(initAr, 0, startPoint, 1, p);
        System.arraycopy(initMa, 0, startPoint, 1 + p, q);
        if (numExog > 0) System.arraycopy(initExog, 0, startPoint, 1 + p + q, numExog);

        MultivariateFunction objective = point -> {
            double sumSq = 0.0;
            double[] errs = new double[maxMaLag > 0 ? maxMaLag : 1];
            int errIdx = 0;
            for (int t = maxLag; t < n; t++) {
                double pred = point[0];
                for (int i = 0; i < p; i++) {
                    pred += point[1 + i] * y[t - arLags[i]];
                }
                for (int i = 0; i < q; i++) {
                    int lag = maLags[i];
                    if (t - lag >= maxLag) {
                        int idx = (errIdx - lag) % maxMaLag;
                        if (idx < 0) idx += maxMaLag;
                        pred += point[1 + p + i] * errs[idx];
                    }
                }
                for (int j = 0; j < numExog; j++) {
                    pred += point[1 + p + q + j] * exog[t][j];
                }
                double error = y[t] - pred;
                if (maxMaLag > 0) {
                    errs[errIdx] = error;
                    errIdx = (errIdx + 1) % maxMaLag;
                }
                sumSq += error * error;
            }
            return sumSq;
        };

        // Только Nelder-Mead
        NelderMeadSimplex simplex = new NelderMeadSimplex(paramCount);
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-12, 1e-12);
        PointValuePair result = optimizer.optimize(
                new ObjectiveFunction(objective),
                new MaxEval(maxEvaluations),
                new InitialGuess(startPoint),
                simplex,
                GoalType.MINIMIZE
        );

        double[] opt = result.getPoint();
        intercept = opt[0];
        arCoeffs = Arrays.copyOfRange(opt, 1, 1 + p);
        maCoeffs = Arrays.copyOfRange(opt, 1 + p, 1 + p + q);
        exogCoeffs = (numExog > 0) ? Arrays.copyOfRange(opt, 1 + p + q, opt.length) : new double[0];

        lastErrors = new LinkedList<>();
        double[] finalErrs = new double[maxMaLag > 0 ? maxMaLag : 1];
        int fErrIdx = 0;
        double sumSq = 0.0;
        for (int t = maxLag; t < n; t++) {
            double pred = intercept;
            for (int i = 0; i < p; i++) pred += arCoeffs[i] * y[t - arLags[i]];
            for (int i = 0; i < q; i++) {
                int lag = maLags[i];
                if (t - lag >= maxLag) {
                    int idx = (fErrIdx - lag) % maxMaLag;
                    if (idx < 0) idx += maxMaLag;
                    pred += maCoeffs[i] * finalErrs[idx];
                }
            }
            for (int j = 0; j < numExog; j++) pred += exogCoeffs[j] * exog[t][j];
            double error = y[t] - pred;
            if (maxMaLag > 0) {
                finalErrs[fErrIdx] = error;
                fErrIdx = (fErrIdx + 1) % maxMaLag;
            }
            sumSq += error * error;
        }
        sigma2 = sumSq / (n - maxLag - paramCount);

        if (q > 0) {
            for (int i = 0; i < q; i++) {
                int idx = (fErrIdx - maLags[i]) % maxMaLag;
                if (idx < 0) idx += maxMaLag;
                lastErrors.addLast(finalErrs[idx]);
            }
        }
    }

    public double[] getArCoeffs() { return arCoeffs; }
    public double[] getMaCoeffs() { return maCoeffs; }
    public double[] getExogCoeffs() { return exogCoeffs; }
    public double getIntercept() { return intercept; }
    public double getSigma2() { return sigma2; }
    public LinkedList<Double> getLastErrors() { return lastErrors; }
}
