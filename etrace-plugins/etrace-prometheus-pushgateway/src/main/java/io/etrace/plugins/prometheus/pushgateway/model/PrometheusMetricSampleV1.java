package io.etrace.plugins.prometheus.pushgateway.model;

import java.util.ArrayList;
import java.util.List;

public class PrometheusMetricSampleV1 implements PrometheusMetricSample {

    public final String name;
    public final List<String> labelNames;
    // Must have same length as labelNames.
    public final double value;
    public final List<String> labelValues;
    // It's an epoch format with milliseconds value included (this field is subject to change).
    public final Long timestampMs;

    public PrometheusMetricSampleV1(String name, List<String> labelNames, List<String> labelValues, double value,
                                    Long timestampMs) {
        this.name = name;
        if (null == labelNames) {
            labelNames = new ArrayList<>();
        }
        this.labelNames = labelNames;
        if (null == labelValues) {
            labelValues = new ArrayList<>();
        }
        this.labelValues = labelValues;
        this.value = value;
        this.timestampMs = timestampMs;
    }
}
