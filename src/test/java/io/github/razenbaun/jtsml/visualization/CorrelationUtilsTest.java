package io.github.razenbaun.jtsml.visualization;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CorrelationUtilsTest {

    @Test
    void testACFWithPositiveCorrelation() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        double[] acf = CorrelationUtils.acf(data, 2);
        assertEquals(1.0, acf[0], 1e-9);
        // ACF(1) для линейного тренда ~0.9, порог 0.5 безопасен
        assertTrue(acf[1] > 0.5);
    }

    @Test
    void testPACFWithLagZeroAndOne() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        double[] pacf = CorrelationUtils.pacf(data, 2);
        assertEquals(1.0, pacf[0], 1e-9);
        assertTrue(Math.abs(pacf[1]) <= 1.0);
    }

    @Test
    void testPACFLength() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        int maxLag = 4;
        double[] pacf = CorrelationUtils.pacf(data, maxLag);
        assertEquals(maxLag + 1, pacf.length);
    }
}
