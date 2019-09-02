package io.etrace.common.modal.metric;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public abstract class AbstractEmpty<M> implements Metric<M> {
    private static final MetricKey key = new MetricKey();

    @Override
    public String getName() {
        return "";
    }

    public String getTopic() {
        return "";
    }

    @Override
    public MetricKey getKey() {
        return key;
    }

    @Override
    public MetricKey getTagKey() {
        return key;
    }

    public M setTopic(String topic) {
        return (M)this;
    }

    @Override
    public M addTag(String key, String value) {
        return (M)this;
    }

    @Override
    public void merge(Metric metric) {
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
    }
}
