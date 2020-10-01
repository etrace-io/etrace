package io.etrace.stream.container.exporter.kafka.model;

public interface HashStrategy {

    int hash(Object key, Object value);
}
