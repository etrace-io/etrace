package io.etrace.common.modal.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.histogram.BucketFunction;
import io.etrace.common.histogram.Distribution;
import io.etrace.common.modal.metric.AbstractMetric;
import io.etrace.common.modal.metric.Histogram;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricType;

import java.io.IOException;

public class HistogramImpl extends AbstractMetric<Histogram> implements Histogram {
    private Distribution distribution;

    public HistogramImpl(BucketFunction bucketFunction) {
        distribution = new Distribution(bucketFunction);
    }

    public void build(TimerImpl timer) {
        super.build(timer);
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Histogram;
    }

    @Override
    public void merge(Metric metric) {
        if (metric instanceof TimerImpl) {
            record(((TimerImpl)metric).getSum());
        } else if (metric instanceof HistogramImpl) {
            distribution.merge(((HistogramImpl)metric).distribution);
        }
    }

    @Override
    public void record(long amount) {
        distribution.record(amount);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeNumber(distribution.getBaseNumber());
        generator.writeNumber(distribution.getMin());
        generator.writeNumber(distribution.getMax());
        generator.writeNumber(distribution.getSum());
        generator.writeNumber(distribution.getCount());
        generator.writeNumber(distribution.getDistributionType().code());
        generator.writeStartArray();
        long[] values = distribution.getValues();
        for (long value : values) {
            generator.writeNumber(value);
        }
        generator.writeEndArray();
    }
}
