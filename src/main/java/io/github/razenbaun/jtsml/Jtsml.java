package io.github.razenbaun.jtsml;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import io.github.razenbaun.jtsml.models.*;
import io.github.razenbaun.jtsml.adapters.ProphetAdapter;
import io.github.razenbaun.jtsml.adapters.ProphetAdapterWithDates;
import io.github.razenbaun.jtsml.visualization.TimeSeriesChart;
import io.github.razenbaun.jtsml.visualization.TimeSeriesChart.Pair;

import java.util.*;

public class Jtsml {
    public static Builder analyze() {
        return new Builder();
    }

    public static class Builder {
        private List<Double> data;
        private List<String> dates;
        private int horizon = 30;
        private List<TimeSeriesModel> models = new ArrayList<>();
        private boolean showChart = false;
        private String chartTitle = "Прогноз временного ряда";
        private boolean autoArimaRequested = false;
        private final Map<TimeSeriesModel, List<String>> modelsWithDates = new LinkedHashMap<>();
        private boolean autoSarimaRequested = false;
        private int seasonalPeriodForAutoSarima = 7;

        public Builder data(List<Double> data) {
            this.data = data;
            return this;
        }

        public Builder dates(List<String> dates) {
            this.dates = dates;
            return this;
        }

        public Builder horizon(int days) {
            this.horizon = days;
            return this;
        }

        public Builder model(TimeSeriesModel model) {
            this.models.add(model);
            return this;
        }

        public Builder modelWithDates(TimeSeriesModel model, List<String> dates) {
            this.modelsWithDates.put(model, dates);
            this.models.add(model); // тоже добавим в общий список для обработки
            return this;
        }

        public Builder autoSarima(int seasonalPeriod) {
            this.seasonalPeriodForAutoSarima = seasonalPeriod;
            this.autoSarimaRequested = true;
            return this;
        }

        public Builder models(String... names) {
            for (String name : names) {
                switch (name.toLowerCase()) {
                    case "naive":
                        models.add(new NaiveModel()); break;
                    case "exp": case "brown":
                        models.add(new ExponentialSmoothingModel()); break;
                    case "linear": case "trend":
                        models.add(new LinearTrendModel()); break;
                    case "arima":
                        models.add(new ArimaModel()); break;
                    case "sarima":
                        models.add(new SARIMAModel(1,0,1,1,0,1,7)); break;
                    case "autoarima":
                        autoArimaRequested = true;
                        break;
                    default:
                        System.err.println("Unknown model: " + name);
                }
            }
            return this;
        }

        public Builder withChart(boolean show) {
            this.showChart = show;
            return this;
        }

        public Builder chartTitle(String title) {
            this.chartTitle = title;
            return this;
        }

        public Forecast predict() {
            if (data == null || data.isEmpty()) {
                throw new IllegalArgumentException("Data must be provided");
            }
            if (models.isEmpty()) {
                models("naive", "exp", "linear", "arima");
            }

            // Обработка авто-ARIMA
            if (autoArimaRequested) {
                ArimaModel auto = null;
                try {
                    auto = AutoARIMA.findBest(data, 3, 2, 3);
                } catch (Exception e) {
                    System.err.println("AutoARIMA failed: " + e.getMessage());
                }
                if (auto != null) {
                    models.add(auto);
                } else {
                    System.err.println("AutoARIMA fallback ARIMA(1,0,0)");
                    models.add(new ArimaModel(1, 0, 0));
                }
                autoArimaRequested = false;
            }

            // Обработка авто-SARIMA
            if (autoSarimaRequested && data != null) {
                try {
                    SARIMAModel autoSarima = AutoSARIMA.findBest(
                            data, 3,2,3, 2,1,2, seasonalPeriodForAutoSarima);
                    if (autoSarima != null) {
                        models.add(autoSarima);
                    } else {
                        System.err.println("AutoSARIMA found no model, using simple SARIMA");
                        models.add(new SARIMAModel(1,0,1,1,0,1, seasonalPeriodForAutoSarima));
                    }
                } catch (Exception e) {
                    System.err.println("AutoSARIMA failed: " + e.getMessage());
                    models.add(new SARIMAModel(1,0,1,1,0,1, seasonalPeriodForAutoSarima));
                }
                autoSarimaRequested = false;
            }

            int trainSize = (int)(data.size() * 0.8);
            List<Double> trainValues = data.subList(0, trainSize);
            List<Double> testValues = data.subList(trainSize, data.size());

            List<String> trainDates = null;
            if (dates != null && dates.size() == data.size()) {
                trainDates = dates.subList(0, trainSize);
            }

            Map<String, List<Double>> allForecasts = new LinkedHashMap<>();
            Map<String, Double> errors = new HashMap<>();

            for (TimeSeriesModel model : models) {
                try {
                    // Обучение с датами или без
                    if (modelsWithDates.containsKey(model)) {
                        // Эта модель должна обучаться с датами
                        if (model instanceof ProphetAdapterWithDates) {
                            ProphetAdapterWithDates pw = (ProphetAdapterWithDates) model;
                            List<String> fullDates = modelsWithDates.get(model);
                            // передаём только train-часть дат
                            List<String> trainD = fullDates.subList(0, trainSize);
                            pw.fit(trainValues, trainD);
                        } else {
                            throw new RuntimeException("modelWithDates not supported for " + model.getName());
                        }
                    } else {
                        model.fit(trainValues);
                    }

                    List<Double> forecast = model.predict(horizon);
                    allForecasts.put(model.getName(), forecast);

                    if (!testValues.isEmpty()) {
                        List<Double> backcast = model.predict(Math.min(testValues.size(), horizon));
                        double mae = 0.0;
                        int len = Math.min(testValues.size(), backcast.size());
                        for (int i = 0; i < len; i++) {
                            mae += Math.abs(testValues.get(i) - backcast.get(i));
                        }
                        errors.put(model.getName(), mae / len);
                    } else {
                        errors.put(model.getName(), Double.NaN);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка модели " + model.getName() + ": " + e.getMessage());
                }
            }

            String bestModelName = null;
            double bestError = Double.MAX_VALUE;
            for (Map.Entry<String, Double> e : errors.entrySet()) {
                if (!Double.isNaN(e.getValue()) && e.getValue() < bestError) {
                    bestError = e.getValue();
                    bestModelName = e.getKey();
                }
            }
            if (bestModelName == null && !allForecasts.isEmpty()) {
                bestModelName = allForecasts.keySet().iterator().next();
                bestError = 0.0;
            }

            Forecast result = new Forecast(allForecasts, bestModelName, bestError, errors);

            if (showChart) {
                List<Pair<String, List<Double>>> seriesList = new ArrayList<>();
                seriesList.add(Pair.of("История", data));
                for (Map.Entry<String, List<Double>> entry : allForecasts.entrySet()) {
                    List<Double> combined = new ArrayList<>(trainValues);
                    combined.addAll(entry.getValue());
                    seriesList.add(Pair.of(entry.getKey(), combined));
                }
                TimeSeriesChart.display(chartTitle, "Время", "Значение",
                        seriesList.toArray(new Pair[0]));
            }

            return result;
        }
    }

    public static class Forecast {
        private final Map<String, List<Double>> predictions;
        private final String bestModel;
        private final double bestError;
        private final Map<String, Double> allErrors;

        public Forecast(Map<String, List<Double>> predictions, String bestModel, double bestError, Map<String, Double> allErrors) {
            this.predictions = predictions;
            this.bestModel = bestModel;
            this.bestError = bestError;
            this.allErrors = allErrors;
        }

        public Map<String, Double> getAllErrors() { return allErrors; }


        public List<Double> getPrediction(String modelName) {
            return predictions.get(modelName);
        }

        public Map<String, List<Double>> getAllPredictions() { return predictions; }
        public String getBestModel() { return bestModel; }
        public double getBestError() { return bestError; }
    }
}