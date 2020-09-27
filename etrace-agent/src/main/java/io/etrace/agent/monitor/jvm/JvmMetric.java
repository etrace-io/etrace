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

package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.HeartBeatConstants;

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

    public void put(JVMMetricType key, long value) {
        metrics.put(buildKey(key), String.valueOf(value));
    }

    public void put(JVMMetricType key, String subType, String name, long value) {
        metrics.put(buildKey(key, subType, name), String.valueOf(value));
    }

    public void put(String key, long value) {
        metrics.put(key, String.valueOf(value));
    }

    public void put(JVMMetricType key, double value) {
        metrics.put(buildKey(key), String.valueOf(value));
    }

    private String buildKey(JVMMetricType JVMMetricType) {
        return type + HeartBeatConstants.TYPE_DELIMIT + JVMMetricType.toString().replace("_",
            HeartBeatConstants.TYPE_DELIMIT)
            .toLowerCase();
    }

    private String buildKey(JVMMetricType JVMMetricType, String subType, String name) {
        return type + HeartBeatConstants.TYPE_DELIMIT + JVMMetricType.toString().replace("_",
            HeartBeatConstants.TYPE_DELIMIT)
            .toLowerCase() + HeartBeatConstants.TYPE_DELIMIT + subType
            + HeartBeatConstants.TYPE_DELIMIT + name;
    }

    public enum JVMMetricType {
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
