package io.etrace.stream.aggregator.function;

import io.etrace.common.message.metric.field.MetricKey;
import io.etrace.stream.aggregator.annotation.UserDefineFunction;

import java.util.List;
import java.util.Map;

public class GroupByObjects {

    @UserDefineFunction(name = "metric_key")
    public static MetricKey GroupByHashKey(Object... objects) {
        MetricKey metricKey = new MetricKey();
        for (Object object : objects) {
            if (object == null) {
                continue;
            }
            if (object instanceof List) {
                List list = (List)object;
                for (Object o : list) {
                    metricKey.add(o.toString());
                }
            } else if (object instanceof Map) {
                Map<String, String> map = (Map<String, String>)object;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    metricKey.add(entry.getKey());
                    metricKey.add(entry.getValue());
                }
            } else if (object instanceof String) {
                metricKey.add((String)object);
            } else {
                metricKey.add(object.toString());
            }
        }
        return metricKey;
    }
}
