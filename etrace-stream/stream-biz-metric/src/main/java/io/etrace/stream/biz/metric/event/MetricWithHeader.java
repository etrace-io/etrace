package io.etrace.stream.biz.metric.event;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.MetricHeader;

public class MetricWithHeader {
    private MetricHeader header;
    private Metric metric;

    public MetricWithHeader() {
    }

    public MetricWithHeader(MetricHeader header, Metric metric) {
        this.header = header;
        this.metric = metric;
    }

    public MetricHeader getHeader() {
        return header;
    }

    public void setHeader(MetricHeader header) {
        this.header = header;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    @Override
    public String toString() {
        return "MetricV2{" +
            "header=" + header +
            ", metric=" + metric +
            '}';
    }
}
