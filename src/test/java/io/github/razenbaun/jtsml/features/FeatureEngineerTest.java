package io.github.razenbaun.jtsml.features;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FeatureEngineerTest {

    @Test
    void testCreateFeaturesWithLags() {
        List<Double> series = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        List<Integer> lags = List.of(1, 2);
        FeatureSet fs = FeatureEngineer.createFeatures(series, lags, 0, false);
        assertEquals(4, fs.rows());
        assertEquals(2, fs.cols());
        double[] firstRow = fs.getFeatures()[0];
        assertEquals(2.0, firstRow[0]); // lag1 = series[1]
        assertEquals(1.0, firstRow[1]); // lag2 = series[0]
    }

    @Test
    void testCreateFeaturesWithRollingMean() {
        List<Double> series = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Integer> lags = List.of(1);
        FeatureSet fs = FeatureEngineer.createFeatures(series, lags, 2, false);
        assertEquals(3, fs.rows());
        double[][] features = fs.getFeatures();
        assertEquals(1.5, features[0][1], 0.001);
        assertEquals(2.5, features[1][1], 0.001);
    }

    @Test
    void testIncludeTimeIndex() {
        List<Double> series = List.of(5.0, 5.5, 6.0, 6.5);
        List<Integer> lags = List.of(1);
        FeatureSet fs = FeatureEngineer.createFeatures(series, lags, 0, true);
        assertEquals(3, fs.rows());
        assertEquals(1.0, fs.getFeatures()[0][1], 0.001);
        assertEquals(2.0, fs.getFeatures()[1][1], 0.001);
    }
}
