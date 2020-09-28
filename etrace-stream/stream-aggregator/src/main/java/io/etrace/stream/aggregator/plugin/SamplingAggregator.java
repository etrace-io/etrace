package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.Pair;
import io.etrace.stream.core.util.ObjectUtil;

/**
 * sampling('Gauge', timestamp, msg)
 * <p>
 * sampling('Counter', msg)
 * <p>
 * sampling('Timer', max, msg)
 * <p>
 * sampling('Histogram', max, msg) // 保持一致  MetricType 全部使用Enum的字面值
 */
public class SamplingAggregator implements AggregationMethod {
    private String sampling;
    private long max = Long.MIN_VALUE;
    private long timestamp;
    private MetricType type;

    @Override
    public void enter(Object value) {
        if (!(value instanceof Object[])) {
            return;
        }

        Object[] values = (Object[])value;

        if (!(values[0] instanceof String)) {
            return;
        }
        if (type == null) {
            String typeStr = (String)values[0];
            //todo zun.li  ??为什么这里是字符串呢？
            MetricType metricType = MetricType.fromIdentifier(typeStr.toLowerCase());
            if (metricType == null) {
                return;
            }
            type = metricType;
        }
        switch (type) {
            case Gauge:
                if (values.length == 3 && values[2] instanceof String) {
                    long newTimestamp = ObjectUtil.toLong(values[1]);
                    if (newTimestamp >= timestamp) {
                        timestamp = newTimestamp;
                        sampling = (String)values[2];
                    }
                }
                break;
            case Counter:
                if (sampling == null && values[1] instanceof String) {
                    sampling = (String)values[1];
                    return;
                }
                break;
            case Timer:
            case Payload:
            case Histogram:
                if (values[2] instanceof String) {
                    long val = ObjectUtil.toLong(values[1]);
                    if (val > max) {
                        max = val;
                        sampling = (String)values[2];
                    }
                }
                break;
        }
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
