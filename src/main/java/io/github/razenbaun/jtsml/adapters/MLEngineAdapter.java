package io.github.razenbaun.jtsml.adapters;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MLEngineAdapter implements TimeSeriesModel, Serializable {
    private static final long serialVersionUID = 1L;
    private final String modelName;
    private boolean fitted = false;

    public MLEngineAdapter(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public void fit(List<Double> trainData) {
        fitted = true;
        System.out.println("[Adapter] " + modelName + " trained on " + trainData.size() + " points");
    }

    @Override
    public List<Double> predict(int horizon) {
        if (!fitted) throw new IllegalStateException("Model not fitted");
        return new ArrayList<>(Collections.nCopies(horizon, 0.0));
    }

    @Override
    public String getName() { return "MLAdapter(" + modelName + ")"; }
}
