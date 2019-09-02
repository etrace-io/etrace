package io.etrace.agent.message.metric;

import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.message.ConfigManger;
import io.etrace.common.message.MetricManager;
import io.etrace.common.modal.metric.Metric;

public class DefaultMetricManager implements MetricManager {
    private MetricQueue metricQueue;
    private ConfigManger configManger;

    @Inject
    public DefaultMetricManager(MetricQueue metricQueue, ConfigManger configManger) {
        this.metricQueue = metricQueue;
        this.configManger = configManger;
    }

    @Override
    public ConfigManger getConfigManager() {
        return configManger;
    }

    @Override
    public void addMetric(Metric metric) {
        metricQueue.produce(metric);
    }

    @Override
    public String getAppId() {
        return AgentConfiguration.getAppId();
    }
}
