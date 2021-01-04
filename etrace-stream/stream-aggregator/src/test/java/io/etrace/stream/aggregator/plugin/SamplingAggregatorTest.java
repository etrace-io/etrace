package io.etrace.stream.aggregator.plugin;

import io.etrace.common.message.metric.Metric;
import io.etrace.stream.aggregator.AbstractEPTest;
import io.etrace.stream.aggregator.mock.MemoryStore;
import io.etrace.stream.aggregator.mock.MockEvent;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

// todo:
@Ignore("run two test case subsequently will fail!")
public class SamplingAggregatorTest extends AbstractEPTest {

    @Test
    public void aggregator() throws Exception {
        epEngine.deployModules(newArrayList("plugin_sampling.sql"));

        agg(1000);

        epEngine.flush();

        Thread.sleep(1000); // wait time window finish
        assertEquals("name-999", ((Metric)MemoryStore.getEvent("gauge")).getSampling());
        assertEquals("name-0", ((Metric)MemoryStore.getEvent("counter")).getSampling());
        assertEquals("name-999", ((Metric)MemoryStore.getEvent("timer")).getSampling());
        assertEquals("name-999", ((Metric)MemoryStore.getEvent("payload")).getSampling());
        assertEquals("name-999", ((Metric)MemoryStore.getEvent("histogram")).getSampling());

        MemoryStore.clear();
    }

    private void agg(int size) {
        for (int i = 0; i < size; i++) {
            MockEvent event = new MockEvent();
            event.setName("name-" + i);
            event.setValue(i);
            event.setTime(System.currentTimeMillis());
            epEngine.sendEvent(event);
        }
    }

    @Test
    public void aggregatorValue0() throws Exception {
        epEngine.deployModules(newArrayList("plugin_sampling.sql"));

        agg(1);

        epEngine.flush();

        Thread.sleep(1000); // wait time window finish
        assertEquals("name-0", ((Metric)MemoryStore.getEvent("gauge")).getSampling());
        assertEquals("name-0", ((Metric)MemoryStore.getEvent("counter")).getSampling());
        assertEquals("name-0", ((Metric)MemoryStore.getEvent("timer")).getSampling());
        assertEquals("name-0", ((Metric)MemoryStore.getEvent("payload")).getSampling());
        assertEquals("name-0", ((Metric)MemoryStore.getEvent("histogram")).getSampling());

        MemoryStore.clear();
    }
}