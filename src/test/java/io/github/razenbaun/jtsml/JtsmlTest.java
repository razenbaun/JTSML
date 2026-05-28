package io.github.razenbaun.jtsml;

import io.github.razenbaun.jtsml.models.ArimaModel;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JtsmlTest {

    @Test
    void testSimplePredictionWithBuilder() {
        List<Double> data = List.of(
                10.0, 12.0, 11.5, 13.0, 14.0, 13.5, 15.0, 16.0, 15.5, 17.0,
                18.0, 17.5, 19.0, 20.0, 19.5
        );
        Jtsml.Forecast forecast = Jtsml.analyze()
                .data(data)
                .horizon(3)
                .models("naive", "exp", "linear")
                .predict();

        assertNotNull(forecast);
        assertNotNull(forecast.getBestModel());
        assertFalse(forecast.getAllPredictions().isEmpty());
        assertNotNull(forecast.getAllErrors());
    }

//    @Test
//    void testPredictWithCustomModel() {
//        List<Double> data = List.of(
//                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0,
//                11.0, 12.0, 13.0, 14.0, 15.0
//        );
//        ArimaModel arima = new ArimaModel(1, 0, 0);
//        Jtsml.Forecast forecast = Jtsml.analyze()
//                .data(data)
//                .model(arima)
//                .horizon(2)
//                .predict();
//
//        List<Double> pred = forecast.getPrediction("ARIMA(1,0,0)");
//        assertNotNull(pred);
//        assertEquals(2, pred.size());
//    }

    @Test
    void testAutoArimaIntegration() {
        List<Double> data = List.of(
                10.0, 11.0, 10.5, 12.0, 11.5, 13.0, 12.5, 14.0, 13.5, 15.0,
                14.5, 16.0, 15.5, 17.0, 16.5
        );
        Jtsml.Forecast forecast = Jtsml.analyze()
                .data(data)
                .models("autoarima")
                .horizon(3)
                .predict();

        assertNotNull(forecast.getBestModel());
        assertFalse(forecast.getAllPredictions().isEmpty());
    }

    @Test
    void testWithChartDoesNotThrow() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        assertDoesNotThrow(() -> {
            Jtsml.analyze()
                    .data(data)
                    .models("naive")
                    .withChart(true)
                    .chartTitle("Test Chart")
                    .horizon(2)
                    .predict();
        });
    }
}
