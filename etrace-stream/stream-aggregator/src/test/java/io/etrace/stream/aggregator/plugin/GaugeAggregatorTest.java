package io.etrace.stream.aggregator.plugin;

import io.etrace.common.message.metric.Metric;
import io.etrace.stream.aggregator.AbstractEPTest;
import io.etrace.stream.aggregator.mock.MemoryStore;
import io.etrace.stream.aggregator.mock.MockEvent;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class GaugeAggregatorTest extends AbstractEPTest {

    @Test
    public void aggregator() throws Exception {
        epEngine.deployModules(newArrayList("plugin_gauge.epl"));

        for (int i = 0; i < 1000; i++) {
            MockEvent event = new MockEvent();
            event.setName("gauge");
            event.setValue(i);
            event.setTime(System.currentTimeMillis());
            epEngine.sendEvent(event);
        }

        epEngine.flush();
        // wait for async MockEPReceiveTask to store results
        Thread.sleep(500);
        assertEquals(999.0, ((Metric)MemoryStore.getEvent("gauge")).getFields().get("gauge").getValue(), 0.0);

        MemoryStore.clear();
    }
}