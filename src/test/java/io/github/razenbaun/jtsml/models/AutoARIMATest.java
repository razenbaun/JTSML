package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AutoARIMATest {

    @Test
    void testFindBestReturnsModel() {
        List<Double> data = List.of(
                10.0, 10.5, 11.0, 10.8, 11.2, 11.5, 11.9, 12.0,
                12.3, 12.6, 12.8, 13.0, 13.2, 13.5, 13.7, 14.0
        );
        ArimaModel best = AutoARIMA.findBest(data, 2, 1, 2);
        assertNotNull(best);
        assertTrue(best.getName().contains("ARIMA"));
        assertDoesNotThrow(() -> best.predict(3));
    }

    @Test
    void testFallbackWhenAllFail() {
        List<Double> data = List.of(1.0, 2.0, 3.0);
        ArimaModel best = AutoARIMA.findBest(data, 2, 1, 2);
        assertNotNull(best);
        assertEquals("ARIMA(1,0,0)", best.getName());
    }
}
