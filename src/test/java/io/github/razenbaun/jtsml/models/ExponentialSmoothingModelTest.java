package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExponentialSmoothingModelTest {

    @Test
    void testFitAndPredict() {
        List<Double> data = List.of(10.0, 11.0, 10.5, 11.5, 12.0);
        ExponentialSmoothingModel model = new ExponentialSmoothingModel(0.5);
        model.fit(data);
        List<Double> forecast = model.predict(3);
        assertEquals(3, forecast.size());
        assertEquals(forecast.get(0), forecast.get(1), 0.001);
    }

    @Test
    void testInvalidAlpha() {
        assertThrows(IllegalArgumentException.class, () -> new ExponentialSmoothingModel(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new ExponentialSmoothingModel(2.0));
    }

    @Test
    void testEmptyDataThrows() {
        ExponentialSmoothingModel model = new ExponentialSmoothingModel();
        assertThrows(IllegalArgumentException.class, () -> model.fit(List.of()));
    }

    @Test
    void testGetNameContainsAlpha() {
        ExponentialSmoothingModel model = new ExponentialSmoothingModel(0.7);
        assertTrue(model.getName().contains("0.7"));
    }
}
