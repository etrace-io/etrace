package io.etrace.stream.aggregator.function;

import io.etrace.common.message.metric.field.MetricKey;
import org.junit.Test;

import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GroupByObjectsTest {

    @Test
    public void testConflict() {
        Map<String, String> map = newHashMap();
        map.put("type", "serviceA0");
        map.put("name", "methodA0");

        Map<String, String> map2 = newHashMap();
        map2.put("type", "serviceB1");
        map2.put("name", "methodB1");

        MetricKey metricKey = GroupByObjects.GroupByHashKey(map);
        MetricKey metricKey2 = GroupByObjects.GroupByHashKey(map2);

        // equals
        assertEquals(Objects.hash(map), Objects.hash(map2));

        //        assertEquals(metricKey.hashCode(), metricKey2.hashCode());
        assertNotEquals(metricKey, metricKey2);
    }

}