package io.etrace.stream.aggregator;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.scopetest.EPAssertionUtil;
import com.espertech.esper.client.scopetest.SupportUpdateListener;
import com.espertech.esper.client.time.CurrentTimeSpanEvent;
import io.etrace.common.constant.FieldName;
import io.etrace.common.exception.EsperConfigException;
import io.etrace.common.histogram.BucketFunction;
import io.etrace.common.histogram.DistAlgorithmBucket;
import io.etrace.common.histogram.Distribution;
import io.etrace.common.histogram.DistributionType;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.Pair;
import io.etrace.stream.aggregator.mock.MockEvent;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.*;

public class EPEngineTest extends AbstractEPTest {

    @Test
    public void createTimeWindowCtx() {
        String name = epEngine.createTimeWindow(10);
        assertEquals("ctx_10sec", name);
        name = epEngine.createTimeWindow(10);
        assertEquals("ctx_10sec", name);
    }

    @Test
    public void initialize() {
        // duplicate initialize
        epEngine.initialize();
    }

    @Test
    public void testMultiFields() throws Exception {
        epEngine.deployModules(newArrayList("metrics.epl"));
        EPStatement statement = epEngine.getStatement("metrics");
        SupportUpdateListener listener = new SupportUpdateListener();
        statement.addListener(listener);

        MockEvent mockEvent1 = new MockEvent();
        mockEvent1.setName("name");
        mockEvent1.setValue(100);
        mockEvent1.setTime(60000);
        mockEvent1.setMsg("xxx");

        epEngine.sendEvent(mockEvent1);

        MockEvent mockEvent2 = new MockEvent();
        mockEvent2.setName("name");
        mockEvent2.setValue(200);
        mockEvent2.setTime(60000);
        mockEvent2.setMsg("yyy");

        epEngine.sendEvent(mockEvent2);

        epEngine.flush();

        assertTrue(listener.isInvoked());

        EPAssertionUtil.assertProps(listener.assertOneGetNew(),
            new String[] {"name", "fsum", "fmin", "fmax", "fgauge", "sampling", "source"}, new Object[] {"name",
                new Field(AggregateType.SUM, 300), new Field(AggregateType.MIN, 100), new Field(AggregateType.MAX, 200),
                new Field(AggregateType.GAUGE, 200),
                new Pair<MetricType, String>(MetricType.Timer, "yyy"), "app_metric"});

    }

    @Test
    public void testMultiFieldsAgg() throws Exception {
        epEngine.deployModules(newArrayList("metrics_agg.epl"));
        EPStatement statement = epEngine.getStatement("metrics_agg");
        SupportUpdateListener listener = new SupportUpdateListener();
        statement.addListener(listener);

        Metric metric = new Metric();
        metric.setMetricName("name");
        metric.setMetricType(MetricType.Timer);
        Map<String, Field> fields = newHashMap();
        fields.put("timerCount", new Field(AggregateType.SUM, 10D));
        fields.put("timerSum", new Field(AggregateType.SUM, 20));
        fields.put("timerMin", new Field(AggregateType.MIN, 1));
        fields.put("timerMax", new Field(AggregateType.MAX, 10));
        metric.setFields(fields);
        metric.setTimestamp(60000);
        metric.setSampling("xxx");
        metric.setSource("app_metric");
        epEngine.sendEvent(metric);

        Metric metric2 = new Metric();
        metric2.setMetricName("name");
        metric2.setMetricType(MetricType.Timer);
        Map<String, Field> fields2 = newHashMap();
        fields2.put("timerCount", new Field(AggregateType.SUM, 10D));
        fields2.put("timerSum", new Field(AggregateType.SUM, 20));
        fields2.put("timerMin", new Field(AggregateType.MIN, 2));
        fields2.put("timerMax", new Field(AggregateType.MAX, 20));
        metric2.setFields(fields2);
        metric2.setTimestamp(60000);
        metric2.setSampling("yyy");
        metric2.setSource("app_metric");
        epEngine.sendEvent(metric2);

        epEngine.sendEvent(new CurrentTimeSpanEvent(System.currentTimeMillis() + 50000));

        epEngine.flush();
        assertTrue(listener.isInvoked());

        Map<String, Field> fieldMap = newHashMap();
        fieldMap.put("timerCount", new Field(AggregateType.SUM, 20));
        fieldMap.put("timerSum", new Field(AggregateType.SUM, 40));
        fieldMap.put("timerMin", new Field(AggregateType.MIN, 1));
        fieldMap.put("timerMax", new Field(AggregateType.MAX, 20));

        // todo here test fail
        EPAssertionUtil.assertProps(listener.assertOneGetNew(), new String[] {"name", "fields", "sampling", "source"},
            new Object[] {"name",
                fieldMap,
                new Pair<MetricType, String>(MetricType.Timer, "yyy"), "app_metric"});

    }

    @Test
    public void testHist() throws Exception {
        epEngine.deployModules(newArrayList("hist.epl"));
        EPStatement statement = epEngine.getStatement("hist");
        SupportUpdateListener listener = new SupportUpdateListener();
        statement.addListener(listener);

        BucketFunction bucketFunction = DistAlgorithmBucket.buildBucketFunction(DistributionType.Percentile, 0);

        int size = 5;

        Distribution[] distributions = new Distribution[size];

        for (int i = 0; i < size; i++) {
            distributions[i] = new Distribution(bucketFunction);
        }

        long ts = System.currentTimeMillis();

        Random random = new Random();
        for (int i = 0; i < 200; i++) {
            long value = random.nextInt(10000);
            MockEvent mockEvent = new MockEvent();
            mockEvent.setTime(ts);
            mockEvent.setValue(value);
            int idx = i % distributions.length;

            mockEvent.addTag("key", "value" + idx);
            mockEvent.setName("name" + idx);
            epEngine.sendEvent(mockEvent);

            distributions[idx].record(value);
        }

        epEngine.flush();

        EventBean[] beans = listener.getLastNewData();

        assertEquals(distributions.length, beans.length);

        Arrays.sort(beans, (b1, b2) -> {
            String name1 = (String)b1.get("name");
            String name2 = (String)b2.get("name");
            return name1.compareTo(name2);
        });

        for (int i = 0; i < distributions.length; i++) {
            Distribution distribution = distributions[i];
            EPAssertionUtil.assertProps(beans[i],
                new String[] {"name", "timerCount", "timerSum", "timerMin", "timerMax"}, new Object[] {
                    "name" + i, new Field(AggregateType.SUM, distribution.getCount()),
                    new Field(AggregateType.SUM, distribution.getSum()),
                    new Field(AggregateType.MIN, distribution.getMin()),
                    new Field(AggregateType.MAX, distribution.getMax())});

            long[] values = distributions[i].getValues();
            Map<String, Field> map = newHashMap();
            for (int j = 0; j < values.length; j++) {
                if (values[j] > 0) {
                    map.put(FieldName.HISTOGRAM_FIELD_PREFIX + j, new Field(AggregateType.SUM, values[j]));
                }
            }

            assertEquals(map, beans[i].get("hist"));
        }

    }

    @Test
    public void testGetTag() throws Exception {
        epEngine.deployModules(newArrayList("get_tag.epl"));
        EPStatement statement = epEngine.getStatement("getTag");
        SupportUpdateListener listener = new SupportUpdateListener();
        statement.addListener(listener);

        MockEvent mockEvent = new MockEvent();
        mockEvent.setName("name");
        mockEvent.addTag("key", "key");
        mockEvent.addTag("tagKey", "tagKey");
        mockEvent.setValue(1);

        epEngine.sendEvent(mockEvent);

        epEngine.flush();

        Map<String, Object> result = newHashMap();
        result.put("name", mockEvent.getName());
        result.put("key", mockEvent.getTag("key"));
        result.put("tagKey", mockEvent.getTag("tagKey"));
        result.put("timerCount", new Field(AggregateType.SUM, 1));
        result.put("timestamp", 0L);

        EPAssertionUtil.assertProps(listener.assertOneGetNew(), result);

    }

    @Test
    public void testSopush() throws Exception {
        epEngine.deployModules(newArrayList("sopush.epl"));
        EPStatement statement = epEngine.getStatement("sopush_request");
        SupportUpdateListener listener = new SupportUpdateListener();
        statement.addListener(listener);

        MockEvent mockEvent = new MockEvent();
        mockEvent.setName("name");
        mockEvent.addTag("project", "project");
        mockEvent.addTag("route", "route");
        mockEvent.addTag("method", "method");
        mockEvent.addTag("status", "status");

        mockEvent.setValue(1);

        epEngine.sendEvent(mockEvent);

        epEngine.flush();

        assertTrue(listener.isInvoked());

        Map<String, Object> result = newHashMap();
        result.put("project", mockEvent.getTag("project"));
        result.put("route", mockEvent.getTag("route"));
        result.put("method", mockEvent.getTag("method"));
        result.put("status", mockEvent.getTag("status"));
        result.put("timestamp", 0L);
        result.put("histogramCount", new Field(AggregateType.SUM, 1));

        EPAssertionUtil.assertProps(listener.assertOneGetNew(), result);
        listener.reset();

        MockEvent mockEvent2 = new MockEvent();
        mockEvent2.setName("name");
        mockEvent2.addTag("project", "project");
        mockEvent2.addTag("error", "error");

        mockEvent2.setValue(1);

        epEngine.sendEvent(mockEvent2);

        epEngine.flush();

        assertNull(listener.getLastNewData());

    }

    @Test
    public void testGroupCount() throws Exception {
        epEngine.deployModules(newArrayList("group_count.sql"));

        assertEquals(0, FlushCounter.get());

        MockEvent event = new MockEvent();
        event.setName("name");

        epEngine.sendEvent(event);

        assertEquals(1, FlushCounter.get());

        MockEvent event2 = new MockEvent();
        event2.setName("name2");

        epEngine.sendEvent(event2);

        assertEquals(2, FlushCounter.get());

        epEngine.sendEvent(event);

        assertEquals(2, FlushCounter.get());

        epEngine.flush();

        assertEquals(0, FlushCounter.get());
    }

    @Test
    public void testCheckGroupBy() throws Exception {

        epEngine.createEPL("epl1",
            "select name, tag('t1'), tags('t2'), tags('t3') as t3, trunc_sec(time,10), trunc_sec(time,10) as ts " +
                ", count(value), sum(value), min(value), max(value) from mock_event group by name, tag('t1'), tags"
                + "('t2'), tags('t3'), trunc_sec(time,10)");

        boolean mark = false;
        try {
            epEngine.createEPL("epl2",
                "select name, tag('t0'), tags('t2'), tags('t3') as t3, trunc_sec(time, 10), trunc_sec(time, 10) as ts "
                    +
                    ", count(value), sum(value), min(value), max(value) from mock_event group by name, tag('t0'), tag"
                    + "('t1'), tags('t2'), tags('t3'), trunc_sec(time, 10)");

            fail("should not happened");
        } catch (EsperConfigException ex) {
            mark = true;
        }

        assertTrue(mark);
    }

    @Test
    public void testCheckMetricAnnotation() throws Exception {
        epEngine.deployModules(newArrayList("annotation_ok_1.epl"));

        boolean mark = false;

        try {
            epEngine.deployModules(newArrayList("annotation_wrong_1.epl"));
            fail();
        } catch (EsperConfigException ex) {
            mark = true;
        }

        assertTrue(mark);
        mark = false;

        try {
            epEngine.deployModules(newArrayList("annotation_wrong_2.epl"));
            fail();
        } catch (EsperConfigException ex) {
            mark = true;
        }

        assertTrue(mark);

    }

    @Test
    public void testCheckFlushEvent() {
        EPStatement statement = epEngine.getStatement("check_flush");
        SupportUpdateListener supportUpdateListener = new SupportUpdateListener();
        statement.addListener(supportUpdateListener);

        CheckFlushEvent checkFlushEvent = new CheckFlushEvent();
        epEngine.sendEvent(checkFlushEvent);

        epEngine.flush();

        EventBean eventBean = supportUpdateListener.assertOneGetNew();
        EPAssertionUtil.assertProps(eventBean, new String[] {"count"}, new Object[] {
            new Field(AggregateType.SUM, 1)
        });
    }

    @Test
    public void testGetValue() throws Exception {
        // 没必要使用 get_value()
        epEngine.deployModules(newArrayList("get_value.epl"));

        EPStatement statement = epEngine.getStatement("get_value");
        SupportUpdateListener supportUpdateListener = new SupportUpdateListener();
        statement.addListener(supportUpdateListener);

        MockEvent mockEvent = new MockEvent();
        mockEvent.setName("name1");

        epEngine.sendEvent(mockEvent);

        MockEvent mockEvent2 = new MockEvent();
        mockEvent2.setName("name2");

        epEngine.sendEvent(mockEvent2);
        epEngine.sendEvent(mockEvent2);

        epEngine.flush();

        EventBean[] beans = supportUpdateListener.getLastNewData();

        Arrays.sort(beans, Comparator.comparing(b -> b.get("name").toString()));

        assertEquals(2, beans.length);

        EPAssertionUtil.assertProps(beans[0], new String[] {"name", "count"},
            new Object[] {"name1", new Field(AggregateType.SUM, 1)});
        EPAssertionUtil.assertProps(beans[1], new String[] {"name", "count"},
            new Object[] {"name2", new Field(AggregateType.SUM, 2)});

    }

}