/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.common.message.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.histogram.BucketFunction;
import io.etrace.common.histogram.Distribution;
import io.etrace.common.message.metric.Histogram;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.field.MetricType;

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
    public void merge(MetricInTraceApi metricInTraceApi) {
        if (metricInTraceApi instanceof TimerImpl) {
            record(((TimerImpl)metricInTraceApi).getSum());
        } else if (metricInTraceApi instanceof HistogramImpl) {
            distribution.merge(((HistogramImpl)metricInTraceApi).distribution);
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
