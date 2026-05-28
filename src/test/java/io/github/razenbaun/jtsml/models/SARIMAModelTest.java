package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SARIMAModelTest {

    @Test
    void testSimpleSARIMAFitPredict() {
        List<Double> data = List.of(
                10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0,
                26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0, 40.0
        );
        SARIMAModel model = new SARIMAModel(1, 0, 0, 0, 0, 0, 1);
        model.fit(data);
        List<Double> forecast = model.predict(2);
        assertEquals(2, forecast.size());
        assertNotNull(forecast);
    }

    @Test
    void testSeasonalSARIMAFitPredict() {
        List<Double> data = List.of(
                10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0,
                11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0,
                12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0, 26.0,
                13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0, 27.0
        );
        SARIMAModel model = new SARIMAModel(1, 0, 1, 1, 0, 1, 4);
        assertDoesNotThrow(() -> model.fit(data));
        List<Double> forecast = model.predict(4);
        assertEquals(4, forecast.size());
    }

//    @Test
//    void testGetAICcReturnsFinite() {
//        List<Double> data = List.of(
//                5.0, 5.5, 5.3, 5.8, 6.0, 6.2, 6.5, 6.7, 7.0, 7.2,
//                7.5, 7.8, 8.0, 8.2, 8.5, 8.7, 9.0, 9.2, 9.5, 9.8
//        );
//        SARIMAModel model = new SARIMAModel(1, 0, 1, 0, 0, 0, 1);
//        model.fit(data);
//        double aicc = model.getAICc();
//        assertTrue(Double.isFinite(aicc));
//        assertTrue(aicc > 0);
//    }
}
