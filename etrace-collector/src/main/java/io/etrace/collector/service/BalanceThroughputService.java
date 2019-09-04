package io.etrace.collector.service;

import io.etrace.common.modal.Collector;

import java.util.Map;

public interface BalanceThroughputService {

    void init();

    void update(long value);

    void bindDefaultClusterCollectors(Map<String, Collector> collectors);
}
