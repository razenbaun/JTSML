package io.github.razenbaun.jtsml.optimization;

import java.util.ArrayList;
import java.util.List;

public class ParameterSpace {
    private final List<Parameter> parameters = new ArrayList<>();

    public ParameterSpace addDouble(String name, double min, double max, double step) {
        parameters.add(new Parameter(name, ParameterType.DOUBLE, min, max, step));
        return this;
    }

    public ParameterSpace addInt(String name, int min, int max, int step) {
        parameters.add(new Parameter(name, ParameterType.INTEGER, min, max, step));
        return this;
    }

    public ParameterSpace addCategorical(String name, Object[] values) {
        parameters.add(new Parameter(name, ParameterType.CATEGORICAL, values));
        return this;
    }

    public List<Parameter> getParameters() { return parameters; }

    public enum ParameterType { DOUBLE, INTEGER, CATEGORICAL }

    public static class Parameter {
        private final String name;
        private final ParameterType type;
        private final double min;
        private final double max;
        private final double step;
        private final Object[] categories;

        public Parameter(String name, ParameterType type, double min, double max, double step) {
            this.name = name;
            this.type = type;
            this.min = min;
            this.max = max;
            this.step = step;
            this.categories = null;
        }

        public Parameter(String name, ParameterType type, Object[] categories) {
            this.name = name;
            this.type = type;
            this.categories = categories;
            this.min = this.max = this.step = 0;
        }

        public String getName() { return name; }
        public ParameterType getType() { return type; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getStep() { return step; }
        public Object[] getCategories() { return categories; }
    }
}
