package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import io.etrace.stream.core.util.ObjectUtil;

public class GaugeAggregator implements AggregationMethod {
    private double gauge;
    private long timestamp;

    @Override
    public void enter(Object value) {
        if (value == null) {
            return;
        }
        if (!value.getClass().isArray()) {
            return;
        }
        Object[] values = (Object[])value;
        if (values.length < 2 || values[0] == null || values[1] == null) {
            return;
        }
        long newTimestamp = ObjectUtil.toLong(values[0]);
        double newValue = ObjectUtil.toDouble(values[1]);
        if (newTimestamp >= timestamp) {
            timestamp = newTimestamp;
            gauge = newValue;
        }
    }

    @Override
    public void leave(Object value) {
        //only context agg do nothing for leave
    }

    @Override
    public Object getValue() {
        return gauge;
    }

    @Override
    public void clear() {
        gauge = 0;
        timestamp = 0;
    }
}
