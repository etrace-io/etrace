package io.etrace.stream.aggregator.annotation;

import com.espertech.esper.event.map.MapEventBean;

public interface MetricProcessor {
    io.etrace.common.message.metric.Metric process(MapEventBean event);
}