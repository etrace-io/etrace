package io.etrace.common.message;

import io.etrace.common.modal.metric.Metric;

public interface MetricManager {

    ConfigManger getConfigManager();

    void addMetric(Metric metric);

    /**
     * metric 集成 alimetrics时，需要在name上带上appId
     *
     * @return
     */
    String getAppId();
}
