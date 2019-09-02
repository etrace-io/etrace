package io.etrace.common.message;

import io.etrace.common.modal.metric.MetricConfig;

public interface ConfigManger {

    void init();

    MetricConfig getMetricConfig();

    boolean isEnabled();

    boolean isAopEnabled();

    default boolean isEsightEnabled() {
        return true;
    }

    int getTagCount();

    int getTagSize();

    int getDataSize();

    int getMessageCount();

    int getRedisSize();

    void shutdown();
}
