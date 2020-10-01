package io.etrace.stream.aggregator.plugin;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static io.etrace.common.constant.FieldName.*;
import static org.junit.Assert.*;

public class MetricSamplingAggregatorTest {

    MetricSamplingAggregator metricSamplingAggregator;

    @Before
    public void init() {
        metricSamplingAggregator = new MetricSamplingAggregator();
    }

    @Test
    public void testCounter() {
        agg(metricSamplingAggregator, MetricType.Counter, null);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Counter, "0");
    }

    @Test
    public void testTimer() {

        agg(metricSamplingAggregator, MetricType.Timer, TIMER_MAX);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Timer, "99");

    }

    @Test
    public void testTimerHistogram() {

        agg(metricSamplingAggregator, MetricType.Timer, HISTOGRAM_MAX);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Timer, "99");

    }

    @Test
    public void testHistogram() {
        agg(metricSamplingAggregator, MetricType.Histogram, HISTOGRAM_MAX);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Histogram, "99");
    }

    @Test
    public void testPayload() {
        agg(metricSamplingAggregator, MetricType.Payload, PAYLOAD_MAX);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Payload, "99");
    }

    @Test
    public void testGauge() {
        agg(metricSamplingAggregator, MetricType.Gauge, null);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Gauge, "99");
    }

    @Test
    public void testCountValue0() {
        aggWithValue(metricSamplingAggregator, MetricType.Counter, null, 0);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Counter, "0");

    }

    @Test
    public void testTimerValue0() {
        aggWithValue(metricSamplingAggregator, MetricType.Timer, TIMER_MAX, 0);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Timer, "0");

    }

    @Test
    public void testGaugeValue0() {
        aggWithValue(metricSamplingAggregator, MetricType.Gauge, null, 0);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Gauge, "0");

    }

    @Test
    public void testPayloadValue0() {
        aggWithValue(metricSamplingAggregator, MetricType.Payload, PAYLOAD_MAX, 0);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Payload, "0");

    }

    @Test
    public void testHistogramValue0() {
        aggWithValue(metricSamplingAggregator, MetricType.Histogram, HISTOGRAM_MAX, 0);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Histogram, "0");

    }

    @Test
    public void testGaugeNegative() {
        aggWithValue(metricSamplingAggregator, MetricType.Gauge, null, -1);

        Object value = metricSamplingAggregator.getValue();

        assertSampling(value, MetricType.Gauge, "-1");

    }

    @Test
    public void testOthers() {
        Set<MetricType> types = newHashSet();
        types.add(MetricType.Metric);
        types.add(MetricType.Ratio);
        types.add(null);

        for (MetricType metricType : types) {
            MetricSamplingAggregator aggregator = new MetricSamplingAggregator();

            agg(aggregator, metricType, null);

            Object value = aggregator.getValue();

            assertTrue(value instanceof Pair);

            Pair pair = (Pair)value;

            assertEquals(metricType, pair.getKey());

            assertNull(((Pair)value).getValue());

        }
    }

    private void agg(MetricSamplingAggregator aggregator, MetricType metricType, String fieldName) {
        for (int i = 0; i < 100; i++) {
            aggWithValue(aggregator, metricType, fieldName, i);
        }
    }

    private void aggWithValue(MetricSamplingAggregator aggregator, MetricType metricType, String fieldName,
                              long value) {
        Metric metric = new Metric();
        metric.setMetricType(metricType);
        metric.addField(fieldName, new Field(null, value));
        metric.setSampling(String.valueOf(value));
        aggregator.enter(metric);

    }

    private void assertSampling(Object value, MetricType metricType, String msg) {
        assertNotNull(value);
        assertTrue(value instanceof Pair);
        Pair pair = (Pair)value;

        assertEquals(metricType, pair.getKey());
        assertEquals(msg, pair.getValue());
    }

}