package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AutoSARIMATest {

    @Test
    void testFindBestReturnsModelOrNull() {
        List<Double> data = List.of(10.0, 12.0, 11.5, 13.0, 14.0, 15.0, 14.5, 16.0, 17.0, 18.0);
        SARIMAModel best = AutoSARIMA.findBest(data, 1, 1, 1, 1, 0, 1, 4);
        if (best != null) {
            assertNotNull(best.getName());
            assertDoesNotThrow(() -> best.predict(2));
        }
    }
}
