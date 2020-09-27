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
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.MetricManager;
import io.etrace.common.message.metric.Payload;
import io.etrace.common.message.metric.field.MetricType;

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
    public void merge(MetricInTraceApi metricInTraceApi) {
        if (metricInTraceApi instanceof PayloadImpl) {
            PayloadImpl payload = (PayloadImpl)metricInTraceApi;
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
