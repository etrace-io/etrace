//package io.etrace.compute.aggregator.task;
//
//import org.junit.Test;
//
//import java.util.List;
//
//
//import io.etrace.compute.aggregator.mock.MockEvent;
//
//import static com.google.common.collect.Lists.newArrayList;
//import static org.junit.Assert.assertNotNull;
//
///**
// * @author io.etrace Date: 2020-04-25 Time: 17:10
// */
//public class AutoFlushTest extends AbstractEPTaskTest {
//    @Override
//    List<String> deployModules() {
//        return newArrayList("group_count.sql");
//    }
//
//    @Test
//    public void testAutoFlush() throws Exception {
//
//        // 不调用flush sleep > 5000  esper数据自动flush
//        for (int i = 0; i < 100; i++) {
//            MockEvent event = new MockEvent();
//            event.setName("name");
//            epTask.handleEvent("key", event);
//        }
//
//
//        Thread.sleep(6000);
//
//        Metric metric = (Metric) MemoryStore.getEvent("name");
//
//        assertNotNull(metric);
//        assertEquals("name", metric.getMetricName());
//        assertEquals(100, metric.getField("timerCount").getValue(), 0.0);
//
//
//    }
//
//
//}
