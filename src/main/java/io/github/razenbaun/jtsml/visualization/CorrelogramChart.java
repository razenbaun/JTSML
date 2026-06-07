package io.github.razenbaun.jtsml.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class CorrelogramChart {

    /**
     * Displays bar correlograms for ACF and PACF.
     * @param title   window title
     * @param lags    array of lags (starting from 0)
     * @param acf     array of ACF values
     * @param pacf    array of PACF values
     */
    public static void display(String title, int[] lags, double[] acf, double[] pacf) {
        JFreeChart chart = createCorrelogramChart(title, lags, acf, pacf);
        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private static JFreeChart createCorrelogramChart(String title, int[] lags,
                                                     double[] acf, double[] pacf) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries acfSeries = new XYSeries("ACF");
        XYSeries pacfSeries = new XYSeries("PACF");

        for (int i = 0; i < lags.length; i++) {
            acfSeries.add(lags[i], acf[i]);
            pacfSeries.add(lags[i], pacf[i]);
        }

        dataset.addSeries(acfSeries);
        dataset.addSeries(pacfSeries);

        JFreeChart chart = ChartFactory.createXYBarChart(
                title, "Lag", false, "Correlation", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setShadowVisible(false);

        return chart;
    }
}
