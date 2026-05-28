package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HoltModelTest {

    @Test
    void testFitWithTrend() {
        List<Double> data = List.of(10.0, 12.0, 14.0, 16.0, 18.0);
        HoltModel model = new HoltModel(0.5, 0.2);
        model.fit(data);
        List<Double> forecast = model.predict(2);
        assertEquals(2, forecast.size());
        assertTrue(forecast.get(1) > forecast.get(0));
    }

    @Test
    void testInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new HoltModel(0.0, 0.1));
        assertThrows(IllegalArgumentException.class, () -> new HoltModel(0.3, 2.5));
    }

    @Test
    void testInsufficientData() {
        HoltModel model = new HoltModel();
        assertThrows(IllegalArgumentException.class, () -> model.fit(List.of(5.0)));
    }
}
