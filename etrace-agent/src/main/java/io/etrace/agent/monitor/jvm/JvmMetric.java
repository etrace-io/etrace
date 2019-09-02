package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.HBConstants;

import java.util.LinkedHashMap;
import java.util.Map;

public class JvmMetric {
    Map<String, String> metrics = new LinkedHashMap<>();
    private String type;

    public JvmMetric(String type) {
        this.type = type;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public void put(MetricType key, long value) {
        metrics.put(buildKey(key), String.valueOf(value));
    }

    public void put(MetricType key, String subType, String name, long value) {
        metrics.put(buildKey(key, subType, name), String.valueOf(value));
    }

    public void put(String key, long value) {
        metrics.put(key, String.valueOf(value));
    }

    public void put(MetricType key, double value) {
        metrics.put(buildKey(key), String.valueOf(value));
    }

    private String buildKey(MetricType metricType) {
        return type + HBConstants.TYPE_DELIMIT + metricType.toString().replace("_", HBConstants.TYPE_DELIMIT)
            .toLowerCase();
    }

    private String buildKey(MetricType metricType, String subType, String name) {
        return type + HBConstants.TYPE_DELIMIT + metricType.toString().replace("_", HBConstants.TYPE_DELIMIT)
            .toLowerCase() + HBConstants.TYPE_DELIMIT + subType
            + HBConstants.TYPE_DELIMIT + name;
    }

    public enum MetricType {
        GARBAGE_COUNT,
        GARBAGE_TIME,
        MEMORY_POOL,

        UPTIME,

        LOADED_CLASSES,

        MEMORY_HEAPUSED,
        MEMORY_HEAPCOMMITTED,
        MEMORY_HEAPMAX,

        MEMORY_NONHEAPUSED,
        MEMORY_NONHEAPCOMMITTED,
        MEMORY_NONHEAPMAX,

        MEMORY_TOTAL,

        OPEN_FILE_DESCRIPTORS,
        MAX_FILE_DESCRIPTORS,
        COMMITTED_VIRTUAL_MEMORY_SIZE,
        FREE_PHYSICAL_MEMORY_SIZE,
        FREE_SWAP_SPACE_SIZE,
        //        PROCESS_CPULOAD,
        CPU_USAGE,

        SYSTEM_CPU_LOAD,
        AVERAGE_SYSTEMLOAD,

        THREAD_THREADS,
        THREAD_DAEMON,
        SUSPENDED_THREADS,

        THREAD_DEADLOCKED,

        DIRECT_BUFFER_POOL_COUNT,
        DIRECT_BUFFER_POOL_MEMORY_USED,

        MAPPED_BUFFER_POOL_COUNT,
        MAPPED_BUFFER_POOL_MEMORY_USED
    }
}
