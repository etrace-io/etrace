package io.etrace.stream.aggregator.mock;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;

import java.util.Collection;
import java.util.Map;

public class MockEPReceiveTask extends DefaultAsyncTask {
    public MockEPReceiveTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
    }

    @Override
    public void processEvent(Object key, Object event) {
        if (event instanceof Collection) {
            Collection events = (Collection)event;
            for (Object o : events) {
                Metric metric = (Metric)o;
                MemoryStore.addEvent(metric.getMetricName(), metric);
            }
        } else {
            Metric metric = (Metric)event;
            MemoryStore.addEvent(metric.getMetricName(), metric);
        }
    }
}
