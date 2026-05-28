package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LinearTrendModelTest {

    @Test
    void testFitAndPredictLinear() {
        List<Double> data = List.of(2.0, 4.0, 6.0, 8.0, 10.0);
        LinearTrendModel model = new LinearTrendModel();
        model.fit(data);
        List<Double> forecast = model.predict(3);
        assertEquals(12.0, forecast.get(0), 0.001);
        assertEquals(14.0, forecast.get(1), 0.001);
        assertEquals(16.0, forecast.get(2), 0.001);
    }

    @Test
    void testLessThanTwoPointsThrows() {
        LinearTrendModel model = new LinearTrendModel();
        assertThrows(IllegalArgumentException.class, () -> model.fit(List.of(5.0)));
    }
}
