package io.etrace.agent.message.event;

import com.lmax.disruptor.EventFactory;
import io.etrace.agent.message.metric.PackageMetric;
import io.etrace.common.modal.metric.MetricKey;

import java.util.Map;

public class PackageEvent {
    private int sendCount;
    private Map<String, Map<MetricKey, PackageMetric>> metrics;

    public void reset(Map<String, Map<MetricKey, PackageMetric>> metrics, int sendCount) {
        this.metrics = metrics;
        this.sendCount = sendCount;
    }

    public Map<String, Map<MetricKey, PackageMetric>> getMetrics() {
        return metrics;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void clear() {
        metrics = null;
        sendCount = 0;
    }

    public static class PackageEventFactory implements EventFactory<PackageEvent> {
        @Override
        public PackageEvent newInstance() {
            return new PackageEvent();
        }
    }
}
