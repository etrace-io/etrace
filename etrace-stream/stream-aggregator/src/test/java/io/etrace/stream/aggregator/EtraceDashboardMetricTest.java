package io.etrace.stream.aggregator;

import io.etrace.common.message.metric.Metric;
import io.etrace.stream.aggregator.mock.MemoryStore;
import io.etrace.stream.aggregator.mock.MockEvent;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class EtraceDashboardMetricTest extends AbstractEPTest {

    /**
     * 事故 metric process 直接使用MapEvent中的Field 多个Metric共享Field 再聚和出错
     */
    @Test
    public void testMultiMetricProcessors() throws Exception {
        epEngine.deployModules(newArrayList("etrace_dashboard.sql"));
        epEngine.flush();

        MockEvent event = new MockEvent();
        event.setName("key1");
        event.setValue(1);
        epEngine.sendEvent(event);

        MockEvent event2 = new MockEvent();
        event2.setName("key2");
        event2.setValue(2);
        epEngine.sendEvent(event2);

        epEngine.flush();
        Thread.sleep(5000);

        assertEquals(1, ((Metric)MemoryStore.getEvent("key1")).getField("count").getValue(), 0.0);
        assertEquals(2, ((Metric)MemoryStore.getEvent("key2")).getField("count").getValue(), 0.0);

        // todo: failed test: 疑似 Metric聚合得不对
        assertEquals(3, ((Metric)MemoryStore.getEvent("dashboard")).getField("count").getValue(), 0.0);
    }
}
