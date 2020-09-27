/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.common.message.metric;

import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Metric extends AbstractMetric {

    private MetricType metricType;
    private String sampling;
    private String source;
    private Map<String, Field> fields;

    public void addField(String fieldKey, Field fieldValue) {
        if (fields == null) {
            fields = new HashMap<>();
        }
        fields.put(fieldKey, fieldValue);
    }

    public Field getField(String fieldKey) {
        return fields.get(fieldKey);
    }

    @Override
    public int calcKey() {
        int result = metricName != null ? metricName.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    @Override
    public Metric cloneMetric() {
        Metric metric = new Metric();
        metric.setMetricName(metricName);
        metric.setTimestamp(timestamp);
        metric.setTags(new HashMap<>(tags));
        metric.setFields(new HashMap<>(fields));
        return metric;
    }
}
