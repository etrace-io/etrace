package io.etrace.common.modal.metric.impl;

import io.etrace.common.modal.metric.AbstractEmpty;
import io.etrace.common.modal.metric.MetricType;
import io.etrace.common.modal.metric.Payload;

public class PayloadEmpty extends AbstractEmpty<Payload> implements Payload {

    @Override
    public void value(long value) {
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Payload;
    }
}
