package io.etrace.stream.aggregator;

import io.etrace.common.pipeline.Component;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;
import static io.etrace.common.constant.InternalMetricName.EP_ENGINE_SEND_EVENT;

public class EPEngineStat {

    private final Map<String, Counter> eventTypeCounters = newConcurrentMap();
    private String taskName;
    private Component component;

    public EPEngineStat(String taskName, Component component) {
        this.taskName = taskName;
        this.component = component;
    }

    public Counter registerEventCounter(String eventType) {
        Counter counter = Counter.builder(EP_ENGINE_SEND_EVENT)
            .tag("pipeline", component.getPipeline())
            .tag("epName", component.getName())
            .tag("task", taskName)
            .tag("eventType", eventType).register(Metrics.globalRegistry);

        eventTypeCounters.put(eventType, counter);
        return counter;
    }

    public Counter getCounter(String eventType) {
        return eventTypeCounters.get(eventType);
    }
}
