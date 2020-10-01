package io.etrace.stream.container.exporter.kafka.model;

import io.etrace.common.message.metric.Metric;
import io.etrace.stream.core.util.MetricUtil;

public class HashingStrategy implements HashStrategy {
    @Override
    public int hash(Object key, Object value) {
        return MetricUtil.hash((Metric)value);
    }
}
