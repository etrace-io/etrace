package io.etrace.agent.message.metric;

import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.message.ConfigManger;
import io.etrace.common.message.MetricManager;
import io.etrace.common.modal.metric.*;
import io.etrace.common.modal.metric.impl.*;

import java.util.Map;

public class MetricProducer {
    private static Counter COUNTER_EMPTY = new CounterEmpty();
    private static Gauge GAUGE_EMPTY = new GaugeEmpty();
    private static Timer TIMER_EMPTY = new TimerEmpty();
    private static Payload PAYLOAD_EMPTY = new PayloadEmpty();

    private MetricManager metricManager;

    @Inject
    public MetricProducer(MetricManager metricManager) {
        this.metricManager = metricManager;
    }

    public Counter newCounter(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return COUNTER_EMPTY;
        }
        return addGlobalTags(new CounterImpl(metricManager, name));
    }

    public Gauge newGauge(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return GAUGE_EMPTY;
        }
        return addGlobalTags(new GaugeImpl(metricManager, name));
    }

    public Timer newTimer(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return TIMER_EMPTY;
        }
        return addGlobalTags(new TimerImpl(metricManager, name));
    }

    public Payload newPayload(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return PAYLOAD_EMPTY;
        }
        return addGlobalTags(new PayloadImpl(metricManager, name));
    }

    /**
     * want to add global tags into metrics, but here is the better place to add. because, once metrics 'completed', we
     * can't add tags any more!
     */
    private <T extends AbstractMetric> T addGlobalTags(T metric) {
        if (AgentConfiguration.getGlobalTags() != null) {
            for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                metric.addTag(entry.getKey(), entry.getValue());
            }
        }
        return metric;
    }
}
