package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ArimaModelTest {

    @Test
    void testFitAndPredictWithStationaryData() {
        List<Double> data = List.of(
                10.0, 10.5, 11.0, 10.8, 11.2, 11.5, 11.9, 12.0, 11.8, 12.2,
                12.5, 12.3, 12.7, 13.0, 12.9, 13.2, 13.1, 13.4, 13.3, 13.6
        );
        ArimaModel model = new ArimaModel(1, 0, 1);
        model.fit(data);
        List<Double> forecast = model.predict(3);
        assertEquals(3, forecast.size());
        assertNotNull(forecast);
        assertDoesNotThrow(() -> model.getAICc());
    }

    @Test
    void testFitAndPredictWithDiff() {
        List<Double> data = List.of(
                100.0, 102.0, 104.0, 106.0, 108.0, 110.0,
                112.0, 114.0, 116.0, 118.0, 120.0, 122.0
        );
        ArimaModel model = new ArimaModel(0, 1, 0);
        model.fit(data);
        List<Double> forecast = model.predict(2);
        assertEquals(2, forecast.size());
        assertEquals(124.0, forecast.get(0), 0.01);
        assertEquals(126.0, forecast.get(1), 0.01);
    }

    @Test
    void testPredictWithoutFitThrows() {
        ArimaModel model = new ArimaModel(1, 0, 0);
        assertThrows(IllegalStateException.class, () -> model.predict(1));
    }

    @Test
    void testNameNotNull() {
        ArimaModel model = new ArimaModel(2, 1, 2);
        assertTrue(model.getName().contains("ARIMA"));
    }
}
