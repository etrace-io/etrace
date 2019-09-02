package io.etrace.agent.message.event;

import com.lmax.disruptor.EventFactory;
import io.etrace.common.modal.metric.Metric;

public class MetricEvent {
    Metric metric;

    public Metric getMetric() {
        return metric;
    }

    public void reset(Metric metric) {
        this.metric = metric;
    }

    public void clear() {
        metric = null;
    }

    public static class MetricEventFactory implements EventFactory<MetricEvent> {
        @Override
        public MetricEvent newInstance() {
            return new MetricEvent();
        }
    }
}
