package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import io.etrace.common.constant.FieldName;
import io.etrace.common.histogram.PercentileBucketFunction;
import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;
import io.etrace.stream.core.util.ObjectUtil;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class HistogramAggregator implements AggregationMethod {
    private static PercentileBucketFunction percentileBucketFunction = PercentileBucketFunction.getFunctions(0);
    // todo replace with IntMap?
    private Map<Integer, Integer> slots;

    @Override
    public void enter(Object value) {
        if (!(value instanceof Number)) {
            return;
        }
        if (slots == null) {
            slots = newHashMap();
        }
        final long current = ObjectUtil.toLong(value);
        int idx = percentileBucketFunction.indexOf(current);
        slots.put(idx, slots.getOrDefault(idx, 0) + 1);
    }

    @Override
    public void leave(Object value) {

    }

    @Override
    public Object getValue() {
        if (slots == null) {
            return null;
        }
        Map<String, Field> fields = newHashMap();
        for (Map.Entry<Integer, Integer> entry : slots.entrySet()) {
            fields.put(FieldName.HISTOGRAM_FIELD_PREFIX + entry.getKey().toString(),
                new Field(AggregateType.SUM, entry.getValue()));
        }
        return fields;
    }

    @Override
    public void clear() {
        slots = null;
    }
}
