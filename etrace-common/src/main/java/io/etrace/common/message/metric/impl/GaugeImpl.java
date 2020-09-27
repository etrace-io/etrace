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
import io.etrace.common.message.metric.Gauge;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.MetricManager;
import io.etrace.common.message.metric.field.MetricType;

import java.io.IOException;

public class GaugeImpl extends AbstractMetric<Gauge> implements Gauge {
    private double value;

    public GaugeImpl(MetricManager metricManager, String name) {
        super(metricManager, name);
    }

    @Override
    public void value(double value) {
        if (!tryCompleted()) {
            return;
        }
        this.value = value;
        if (manager != null) {
            manager.addMetric(this);
        }
    }

    public double getValue() {
        return value;
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Gauge;
    }

    @Override
    public void merge(MetricInTraceApi metricInTraceApi) {
        if (metricInTraceApi instanceof GaugeImpl) {
            GaugeImpl gauge = (GaugeImpl)metricInTraceApi;
            if (gauge.timestamp > timestamp) {
                setTimestamp(gauge.timestamp);
                value = gauge.value;
            }
        }
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeNumber(value);
    }
}
