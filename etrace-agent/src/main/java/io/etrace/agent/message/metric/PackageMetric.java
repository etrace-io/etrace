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

package io.etrace.agent.message.metric;

import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.field.MetricKey;
import io.etrace.common.message.metric.impl.AbstractMetric;
import io.etrace.common.message.metric.impl.HistogramImpl;
import io.etrace.common.message.metric.impl.TimerImpl;

import java.util.HashMap;
import java.util.Map;

public class PackageMetric {
    protected MetricQueue.EventConsumer consumer;
    protected boolean isHistogram;
    protected String topic;
    protected MetricInTraceApi<?> defaultMetric;
    protected Map<MetricKey, MetricInTraceApi<?>> metrics;
    protected long count;
    protected int maxGroup;

    public PackageMetric(ConfigManger configManger, MetricQueue.EventConsumer consumer,
                         MetricInTraceApi<?> metricInTraceApi) {
        if (metricInTraceApi instanceof TimerImpl && ((TimerImpl)metricInTraceApi).isUpperEnable()) {
            isHistogram = true;
            maxGroup = configManger.getMetricConfig().getMaxHistogramGroup();
        } else {
            maxGroup = configManger.getMetricConfig().getMaxGroup();
            isHistogram = false;
        }
        if (metricInTraceApi instanceof AbstractMetric) {
            this.topic = ((AbstractMetric<?>)metricInTraceApi).getTopic();
        }
        this.consumer = consumer;
    }

    private void addTagsMetric(MetricInTraceApi<?> metricInTraceApi) {
        if (metrics == null) {
            metrics = new HashMap<>();
            metrics.put(metricInTraceApi.getTagKey(), buildMetric(metricInTraceApi));
            count++;
            consumer.sendCount++;
            return;
        }
        MetricKey key = metricInTraceApi.getTagKey();
        MetricInTraceApi<?> oldMetricInTraceApi = metrics.get(key);
        if (oldMetricInTraceApi == null) {
            if (count >= maxGroup) {
                return;
            }
            count++;
            consumer.sendCount++;
            metrics.put(key, buildMetric(metricInTraceApi));
        } else {
            oldMetricInTraceApi.merge(metricInTraceApi);
            consumer.mergeCount++;
        }
    }

    private MetricInTraceApi<?> buildMetric(MetricInTraceApi<?> metricInTraceApi) {
        if (isHistogram) {
            HistogramImpl histogram = new HistogramImpl(consumer.getBucketFunction());
            if (metricInTraceApi instanceof TimerImpl) {
                histogram.build((TimerImpl)metricInTraceApi);
                histogram.merge(metricInTraceApi);
            }
            return histogram;
        }
        return metricInTraceApi;
    }

    private void addDefault(MetricInTraceApi<?> metricInTraceApi) {
        if (defaultMetric == null) {
            defaultMetric = buildMetric(metricInTraceApi);
            consumer.sendCount++;
        } else {
            defaultMetric.merge(metricInTraceApi);
            consumer.mergeCount++;
        }
    }

    public void merge(MetricInTraceApi<?> metricInTraceApi) {
        if (metricInTraceApi.getTagKey() == null) {
            addDefault(metricInTraceApi);
        } else {
            addTagsMetric(metricInTraceApi);
        }
    }

    public boolean isEmpty() {
        return (metrics == null || metrics.size() <= 0) && defaultMetric == null;
    }

    public String getTopic() {
        return topic;
    }
}
