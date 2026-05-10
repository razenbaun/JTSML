package io.github.razenbaun.jtsml.features;

import java.util.List;
import java.util.Map;

/**
 * Набор признаков и целевая переменная.
 */
public class FeatureSet {
    private final double[][] features;  // [rows][features]
    private final double[] targets;     // [rows]

    public FeatureSet(double[][] features, double[] targets) {
        this.features = features;
        this.targets = targets;
    }

    public double[][] getFeatures() { return features; }
    public double[] getTargets() { return targets; }
    public int rows() { return features.length; }
    public int cols() { return features[0].length; }
}