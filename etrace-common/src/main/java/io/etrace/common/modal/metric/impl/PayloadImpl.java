package io.etrace.common.modal.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.message.MetricManager;
import io.etrace.common.modal.metric.AbstractMetric;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricType;
import io.etrace.common.modal.metric.Payload;

import java.io.IOException;

public class PayloadImpl extends AbstractMetric<Payload> implements Payload {
    private long sum;
    private long count = 1;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;

    public PayloadImpl(MetricManager metricManager, String name) {
        super(metricManager, name);
    }

    @Override
    public void value(long value) {
        if (!tryCompleted()) {
            return;
        }
        sum = value;
        setMinAndMax(sum);
        if (manager != null) {
            manager.addMetric(this);
        }
    }

    private void setMinAndMax(long sum) {
        setMin(sum);
        setMax(sum);
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Payload;
    }

    @Override
    public void merge(Metric metric) {
        if (metric instanceof PayloadImpl) {
            PayloadImpl payload = (PayloadImpl)metric;
            sum += payload.sum;
            count += payload.count;
            setMin(payload.min);
            setMax(payload.max);
        }
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeNumber(sum);
        generator.writeNumber(count);
        generator.writeNumber(min);
        generator.writeNumber(max);
    }

    public long getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }

    public long getMin() {
        return min;
    }

    private void setMin(long sum) {
        if (this.min > sum) {
            this.min = sum;
        }
    }

    public long getMax() {
        return max;
    }

    private void setMax(long sum) {
        if (this.max < sum) {
            this.max = sum;
        }
    }
}
