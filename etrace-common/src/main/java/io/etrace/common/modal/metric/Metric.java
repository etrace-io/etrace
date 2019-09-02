package io.etrace.common.modal.metric;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface Metric<M> {

    String getName();

    MetricType getMetricType();

    MetricKey getKey();

    MetricKey getTagKey();

    M addTag(String key, String value);

    void merge(Metric metric);

    void write(JsonGenerator generator) throws IOException;
}
