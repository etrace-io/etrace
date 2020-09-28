package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import io.etrace.common.message.metric.field.Field;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class FieldsAggregator implements AggregationMethod {
    private Map<String, Field> fields = null;

    @Override
    public void enter(Object o) {
        if (!(o instanceof Map)) {
            return;
        }
        Map map = (Map)o;
        if (fields == null) {
            fields = newHashMap();
        }
        for (Object entryObj : map.entrySet()) {
            if (entryObj instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry)entryObj;
                if (entry.getKey() instanceof String && entry.getValue() instanceof Field) {
                    String key = (String)entry.getKey();
                    Field field = (Field)entry.getValue();
                    if (!fields.containsKey(key)) {
                        // do not change origin metric field
                        fields.put(key, new Field(field.getAggregateType(), field.getValue()));
                    } else {
                        fields.get(key).merge(field);
                    }
                }
            }
        }
    }

    @Override
    public void leave(Object o) {

    }

    @Override
    public Object getValue() {
        return fields;
    }

    @Override
    public void clear() {
        fields = null;
    }
}

