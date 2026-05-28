package io.github.razenbaun.jtsml.optimization;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import io.github.razenbaun.jtsml.models.ExponentialSmoothingModel;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class HyperparameterOptimizerTest {

    @Test
    void testGridSearchForExponentialSmoothing() {
        List<Double> data = List.of(10.0, 11.0, 10.5, 11.5, 12.0, 12.5, 13.0);
        ParameterSpace space = new ParameterSpace()
                .addDouble("alpha", 0.1, 0.9, 0.4);
        ModelFactory factory = params -> {
            double alpha = (double) params.get("alpha");
            return new ExponentialSmoothingModel(alpha);
        };
        HyperparameterOptimizer.OptimizationResult result =
                HyperparameterOptimizer.optimize(factory, space, data, 2);
        assertNotNull(result.getBestModel());
        assertNotNull(result.getBestParams());
        assertTrue(result.getBestError() >= 0);
    }

    @Test
    void testRandomSearch() {
        List<Double> data = List.of(5.0, 6.0, 5.5, 7.0, 8.0, 8.5);
        ParameterSpace space = new ParameterSpace()
                .addDouble("alpha", 0.1, 0.9, 0.1);
        ModelFactory factory = params -> new ExponentialSmoothingModel((double) params.get("alpha"));
        HyperparameterOptimizer.OptimizationResult result =
                HyperparameterOptimizer.optimize(factory, space, data, 2,
                        HyperparameterOptimizer.SearchMode.RANDOM);
        assertNotNull(result.getBestModel());
    }

    @Test
    void testNoValidModelThrows() {
        List<Double> data = List.of(1.0, 2.0);
        ParameterSpace space = new ParameterSpace()
                .addDouble("alpha", 10.0, 20.0, 10.0);
        ModelFactory factory = params -> new ExponentialSmoothingModel((double) params.get("alpha"));
        assertThrows(RuntimeException.class, () ->
                HyperparameterOptimizer.optimize(factory, space, data, 1));
    }
}
