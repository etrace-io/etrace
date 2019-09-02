package io.etrace.common.modal.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.message.MetricManager;
import io.etrace.common.modal.metric.AbstractMetric;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricType;
import io.etrace.common.modal.metric.Timer;

import java.io.IOException;

public class TimerImpl extends AbstractMetric<Timer> implements Timer {
    private boolean upperEnable;
    private long sum;
    private long count = 1;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;

    public TimerImpl(MetricManager metricManager, String name) {
        super(metricManager, name);
        upperEnable = true;
    }

    @Override
    public void value(long value) {
        if (!tryCompleted()) {
            return;
        }
        sum = value;
        setMinAndMax(sum);
        getKey().add(upperEnable);
        if (manager != null) {
            manager.addMetric(this);
        }
    }

    private void setMinAndMax(long sum) {
        setMin(sum);
        setMax(sum);
    }

    @Override
    public void end() {
        long cast = System.currentTimeMillis() - timestamp;
        value(cast);
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Timer;
    }

    @Override
    public void merge(Metric metric) {
        if (metric instanceof TimerImpl) {
            TimerImpl timer = (TimerImpl)metric;
            sum += timer.sum;
            count += timer.count;
            setMin(timer.min);
            setMax(timer.max);
        }
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeNumber(sum);
        generator.writeNumber(count);
        generator.writeNumber(min);
        generator.writeNumber(max);
        generator.writeNumber(upperEnable ? 1 : 0);
    }

    @Override
    public boolean isUpperEnable() {
        return upperEnable;
    }

    @Override
    public TimerImpl setUpperEnable(boolean upperEnable) {
        this.upperEnable = upperEnable;
        return this;
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
