package io.etrace.common.modal.metric.impl;

import io.etrace.common.modal.metric.AbstractEmpty;
import io.etrace.common.modal.metric.Gauge;
import io.etrace.common.modal.metric.MetricType;

public class GaugeEmpty extends AbstractEmpty<Gauge> implements Gauge {
    @Override
    public void value(double value) {
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Gauge;
    }
}
