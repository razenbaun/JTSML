package io.github.razenbaun.jtsml.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.List;

public class TimeSeriesChart {

    /**
     * Отображает график временного ряда с несколькими сериями.
     * @param title      заголовок окна
     * @param xLabel     подпись оси X
     * @param yLabel     подпись оси Y
     * @param series     пары "название – данные" (x — индекс, y — значение)
     */
    @SafeVarargs
    public static void display(String title, String xLabel, String yLabel,
                               Pair<String, List<Double>>... series) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Pair<String, List<Double>> pair : series) {
            XYSeries xy = new XYSeries(pair.label);
            List<Double> data = pair.data;
            for (int i = 0; i < data.size(); i++) {
                xy.add(i, data.get(i));
            }
            dataset.addSeries(xy);
        }
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false
        );
        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Простейший вспомогательный класс для хранения пары "название–данные".
     */
    public static class Pair<L, R> {
        public final L label;
        public final R data;
        public Pair(L label, R data) {
            this.label = label;
            this.data = data;
        }
        public static <L, R> Pair<L, R> of(L label, R data) {
            return new Pair<>(label, data);
        }
    }
}
