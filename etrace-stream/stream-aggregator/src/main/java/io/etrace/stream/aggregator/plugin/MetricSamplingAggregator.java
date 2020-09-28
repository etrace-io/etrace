package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.Pair;

import static io.etrace.common.constant.FieldName.*;

/**
 * metricSampling(metric)
 * <p>
 * 需要metricType, maxValue, timestamp, sampling确定采样
 * <p>
 * 作为参数传入太多 传入Metric
 * <p>
 * metricSampling(metric) as sampling
 */
public class MetricSamplingAggregator implements AggregationMethod {
    private String sampling;
    private long max = Long.MIN_VALUE;
    private long timestamp;
    private MetricType type;

    @Override
    public void enter(Object value) {
        if (!(value instanceof Metric)) {
            return;
        }

        Metric metric = (Metric)value;

        MetricType type = metric.getMetricType();

        if (type == null) {
            return;
        }

        if (this.type == null || this.type == MetricType.Metric) {
            this.type = type;
        }
        switch (type) {
            case Gauge:
                if (metric.getTimestamp() >= timestamp && metric.getSampling() != null) {
                    timestamp = metric.getTimestamp();
                    sampling = metric.getSampling();
                }
                break;
            case Counter:
                if (sampling == null) {
                    sampling = metric.getSampling();
                }
                break;
            case Timer:
                // 前端根据FieldName判断采样类型
                // 遗留原因 soa_provider等应用层监控 metirc_type采用timer, fieldName却有timerCount, histogramCount, upper
                // 将sql中全部的histogram类型都换成timer
                if (!updateMax(TIMER_MAX
                    , metric)) {
                    updateMax(HISTOGRAM_MAX, metric);
                }
                break;
            case Payload:
                updateMax(PAYLOAD_MAX, metric);
                break;
            case Histogram:
                updateMax(HISTOGRAM_MAX, metric);
                break;
            default:
        }
    }

    /**
     * @return fieldName对应的field是否存在
     */
    private boolean updateMax(String maxFieldName, Metric metric) {
        Field maxField = metric.getFields().get(maxFieldName);
        if (maxField == null) {
            return false;
        }
        double newMax = maxField.getValue();
        String newSampling = metric.getSampling();
        if (newMax > max && newSampling != null) {
            // safe
            max = (long)newMax;
            sampling = newSampling;
        }
        return true;
    }

    @Override
    public void leave(Object value) {
        //do nothing
        clear();
    }

    @Override
    public Object getValue() {
        return new Pair<>(type, sampling);
    }

    @Override
    public void clear() {
        sampling = null;
        type = null;
        timestamp = 0;
        max = Long.MIN_VALUE;
    }
}
