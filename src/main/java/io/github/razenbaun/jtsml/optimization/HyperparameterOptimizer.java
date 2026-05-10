package io.github.razenbaun.jtsml.optimization;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HyperparameterOptimizer {

    public static OptimizationResult optimize(ModelFactory factory, ParameterSpace space,
                                              List<Double> data, int validationSize) {
        return optimize(factory, space, data, validationSize, SearchMode.GRID);
    }

    public static OptimizationResult optimize(ModelFactory factory, ParameterSpace space,
                                              List<Double> data, int validationSize, SearchMode mode) {
        List<Map<String, Object>> combinations;
        if (mode == SearchMode.RANDOM) {
            combinations = generateRandomCombinations(space, 200);
        } else {
            combinations = generateCombinations(space);
        }
        double bestError = Double.MAX_VALUE;
        Map<String, Object> bestParams = null;
        TimeSeriesModel bestModel = null;

        int trainSplit = data.size() - validationSize;
        List<Double> train = data.subList(0, trainSplit);
        List<Double> val = data.subList(trainSplit, data.size());

        for (Map<String, Object> params : combinations) {
            try {
                TimeSeriesModel model = factory.create(params);
                model.fit(train);
                List<Double> forecast = model.predict(val.size());
                double mae = 0.0;
                for (int i = 0; i < val.size(); i++)
                    mae += Math.abs(val.get(i) - forecast.get(i));
                mae /= val.size();
                if (mae < bestError) {
                    bestError = mae;
                    bestParams = params;
                    bestModel = model;
                }
            } catch (Exception e) {
                // пропускаем неудачную комбинацию
            }
        }
        if (bestModel == null) {
            throw new RuntimeException("No valid model found. Check data and parameter space.");
        }
        return new OptimizationResult(bestModel, bestParams, bestError);
    }

    private static List<Map<String, Object>> generateCombinations(ParameterSpace space) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        generate(space.getParameters(), 0, new HashMap<>(), combinations);
        return combinations;
    }

    private static void generate(List<ParameterSpace.Parameter> params, int idx,
                                 Map<String, Object> current,
                                 List<Map<String, Object>> out) {
        if (idx == params.size()) {
            out.add(new HashMap<>(current));
            return;
        }
        ParameterSpace.Parameter p = params.get(idx);
        switch (p.getType()) {
            case DOUBLE:
                for (double val = p.getMin(); val <= p.getMax() + 1e-9; val += p.getStep()) {
                    current.put(p.getName(), val);
                    generate(params, idx + 1, current, out);
                }
                break;
            case INTEGER:
                for (int val = (int)p.getMin(); val <= (int)p.getMax(); val += (int)p.getStep()) {
                    current.put(p.getName(), val);
                    generate(params, idx + 1, current, out);
                }
                break;
            case CATEGORICAL:
                for (Object cat : p.getCategories()) {
                    current.put(p.getName(), cat);
                    generate(params, idx + 1, current, out);
                }
                break;
        }
        current.remove(p.getName());
    }

    private static List<Map<String, Object>> generateRandomCombinations(ParameterSpace space, int count) {
        List<Map<String, Object>> combs = new ArrayList<>();
        List<ParameterSpace.Parameter> params = space.getParameters();
        for (int i = 0; i < count; i++) {
            Map<String, Object> map = new HashMap<>();
            for (ParameterSpace.Parameter p : params) {
                switch (p.getType()) {
                    case DOUBLE:
                        double d = ThreadLocalRandom.current().nextDouble() * (p.getMax() - p.getMin()) + p.getMin();
                        map.put(p.getName(), d);
                        break;
                    case INTEGER:
                        int v = ThreadLocalRandom.current().nextInt((int)p.getMin(), (int)p.getMax() + 1);
                        map.put(p.getName(), v);
                        break;
                    case CATEGORICAL:
                        Object[] cats = p.getCategories();
                        map.put(p.getName(), cats[ThreadLocalRandom.current().nextInt(cats.length)]);
                        break;
                }
            }
            combs.add(map);
        }
        return combs;
    }

    public enum SearchMode { GRID, RANDOM }

    public static class OptimizationResult {
        private final TimeSeriesModel bestModel;
        private final Map<String, Object> bestParams;
        private final double bestError;

        public OptimizationResult(TimeSeriesModel bestModel, Map<String, Object> bestParams, double bestError) {
            this.bestModel = bestModel;
            this.bestParams = bestParams;
            this.bestError = bestError;
        }
        public TimeSeriesModel getBestModel() { return bestModel; }
        public Map<String, Object> getBestParams() { return bestParams; }
        public double getBestError() { return bestError; }
    }
}
