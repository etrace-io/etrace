package io.etrace.common.modal.metric.impl;

import io.etrace.common.modal.metric.AbstractEmpty;
import io.etrace.common.modal.metric.Counter;
import io.etrace.common.modal.metric.MetricType;

public class CounterEmpty extends AbstractEmpty<Counter> implements Counter {
    @Override
    public void once() {
    }

    @Override
    public void value(long count) {
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Counter;
    }
}
