package io.github.razenbaun.jtsml;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JtsmlTest {

    @Test
    void shouldDemonstrateUx() {
        List<Jtsml.SalesRecord> mockData = List.of(
                new Jtsml.SalesRecord("2024-01-01", 100.0),
                new Jtsml.SalesRecord("2024-01-02", 120.0)
        );

        Jtsml.Forecast forecast = Jtsml.analyze()
                .data(mockData)
                .timestampField("date")
                .valueField("amount")
                .horizon(30)
                .models("XGBoost")
                .optimize(true)
                .predict();

        assertNotNull(forecast);
        assertNotNull(forecast.getBestModel());
        assertNotNull(forecast.getPredictions());
        assertFalse(forecast.getMetrics().isEmpty());

        System.out.println(forecast);
    }
}
