package io.github.razenbaun.jtsml.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NaiveModelTest {

    @Test
    void testPredictReturnsLastValue() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        NaiveModel model = new NaiveModel();
        model.fit(data);
        List<Double> forecast = model.predict(4);
        assertEquals(5.0, forecast.get(0));
        assertEquals(5.0, forecast.get(3));
    }

    @Test
    void testEmptyDataThrows() {
        NaiveModel model = new NaiveModel();
        assertThrows(IllegalArgumentException.class, () -> model.fit(List.of()));
    }
}
