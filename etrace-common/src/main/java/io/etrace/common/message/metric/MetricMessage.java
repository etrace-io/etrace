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

package io.etrace.common.message.metric;

import io.etrace.common.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class MetricMessage implements Iterable<Pair<MetricHeader, Metric>> {

    private MetricHeader metricHeader;
    private List<Metric> metrics;

    public MetricMessage() {
    }

    public MetricMessage(MetricHeader metricHeader, List<Metric> metrics) {
        this.metricHeader = metricHeader;
        this.metrics = metrics;
    }

    public MetricHeader getMetricHeader() {
        return metricHeader;
    }

    public void setMetricHeader(MetricHeader metricHeader) {
        this.metricHeader = metricHeader;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(Metric metric) {
        if (metrics == null) {
            metrics = new ArrayList<>();
        }
        metrics.add(metric);
    }

    @Override
    public Iterator<Pair<MetricHeader, Metric>> iterator() {
        return new Iterator<Pair<MetricHeader, Metric>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < metrics.size();
            }

            @Override
            public Pair<MetricHeader, Metric> next() {
                Pair<MetricHeader, Metric> pair = new Pair<>(metricHeader, metrics.get(index));
                index++;
                return pair;
            }
        };
    }

    @Override
    public String toString() {
        return "MetricMessage{" +
            "metricHeader=" + metricHeader +
            ", metrics=" + metrics +
            '}';
    }

}
