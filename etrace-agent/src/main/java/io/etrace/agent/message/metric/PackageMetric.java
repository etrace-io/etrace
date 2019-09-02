package io.etrace.agent.message.metric;

import io.etrace.common.message.ConfigManger;
import io.etrace.common.modal.metric.AbstractMetric;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricKey;
import io.etrace.common.modal.metric.impl.HistogramImpl;
import io.etrace.common.modal.metric.impl.TimerImpl;

import java.util.HashMap;
import java.util.Map;

public class PackageMetric {
    protected MetricQueue.EventConsumer comsumer;
    protected boolean isHistogram;
    protected String topic;
    protected Metric defaultMetric;
    protected Map<MetricKey, Metric> metrics;
    protected long count;
    protected int maxGroup;

    public PackageMetric(ConfigManger configManger, MetricQueue.EventConsumer comsumer, Metric metric) {
        if (metric instanceof TimerImpl && ((TimerImpl)metric).isUpperEnable()) {
            isHistogram = true;
            maxGroup = configManger.getMetricConfig().getMaxHistogramGroup();
        } else {
            maxGroup = configManger.getMetricConfig().getMaxGroup();
            isHistogram = false;
        }
        if (metric instanceof AbstractMetric) {
            this.topic = ((AbstractMetric)metric).getTopic();
        }
        this.comsumer = comsumer;
    }

    private void addTagsMetric(Metric metric) {
        if (metrics == null) {
            metrics = new HashMap<>();
            metrics.put(metric.getTagKey(), buidMetric(metric));
            count++;
            comsumer.sendCount++;
            return;
        }
        MetricKey key = metric.getTagKey();
        Metric oldMetric = metrics.get(key);
        if (oldMetric == null) {
            if (count >= maxGroup) {
                return;
            }
            count++;
            comsumer.sendCount++;
            metrics.put(key, buidMetric(metric));
        } else {
            oldMetric.merge(metric);
            comsumer.mergeCount++;
        }
    }

    private Metric buidMetric(Metric metric) {
        if (isHistogram) {
            HistogramImpl histogram = new HistogramImpl(comsumer.getBucketFunction());
            if (metric instanceof TimerImpl) {
                histogram.build((TimerImpl)metric);
                histogram.merge(metric);
            }
            return histogram;
        }
        return metric;
    }

    private void addDefault(Metric metric) {
        if (defaultMetric == null) {
            defaultMetric = buidMetric(metric);
            comsumer.sendCount++;
        } else {
            defaultMetric.merge(metric);
            comsumer.mergeCount++;
        }
    }

    public void merge(Metric metric) {
        if (metric.getTagKey() == null) {
            addDefault(metric);
        } else {
            addTagsMetric(metric);
        }
    }

    public boolean isEmpty() {
        return (metrics == null || metrics.size() <= 0) && defaultMetric == null;
    }

    public String getTopic() {
        return topic;
    }
}
