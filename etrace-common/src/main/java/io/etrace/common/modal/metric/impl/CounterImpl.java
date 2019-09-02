package io.etrace.common.modal.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.message.MetricManager;
import io.etrace.common.modal.metric.AbstractMetric;
import io.etrace.common.modal.metric.Counter;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricType;

import java.io.IOException;

public class CounterImpl extends AbstractMetric<Counter> implements Counter {
    private long count;

    public CounterImpl(MetricManager metricManager, String name) {
        super(metricManager, name);
    }

    @Override
    public void merge(Metric metric) {
        if (metric instanceof CounterImpl) {
            count += ((CounterImpl)metric).count;
        }
    }

    public long getCount() {
        return count;
    }

    @Override
    public void once() {
        if (!tryCompleted()) {
            return;
        }
        count = 1;
        if (manager != null) {
            manager.addMetric(this);
        }
    }

    @Override
    public void value(long count) {
        if (!tryCompleted()) {
            return;
        }
        this.count = count;
        if (manager != null) {
            manager.addMetric(this);
        }

    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeNumber(count);
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Counter;
    }
}
