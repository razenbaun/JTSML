package io.github.razenbaun.jtsml.optimization;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import java.util.Map;

@FunctionalInterface
public interface ModelFactory {
    TimeSeriesModel create(Map<String, Object> params);
}
