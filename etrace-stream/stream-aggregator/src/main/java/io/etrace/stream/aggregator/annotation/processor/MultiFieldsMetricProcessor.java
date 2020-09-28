package io.etrace.stream.aggregator.annotation.processor;

import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.event.map.MapEventBean;
import com.google.common.base.Strings;
import io.etrace.common.exception.EsperConfigException;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.Pair;
import io.etrace.stream.aggregator.annotation.Metric;
import io.etrace.stream.aggregator.annotation.MetricProcessor;
import io.etrace.stream.aggregator.annotation.ProcessorFor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@ProcessorFor(name = Metric.class)
public class MultiFieldsMetricProcessor extends AbstractProcessor implements MetricProcessor {
    private List<String> fields = newArrayList();
    private Map<String, String> fieldMap = newHashMap();

    private String source;
    private String sampling;

    @Override
    protected void init(AnnotationAttribute attribute) {
        if ("fields".equals(attribute.getName())) {
            Object[] values = (Object[])attribute.getValue();
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    fields.add((String)values[i]);
                }
            }
        } else if ("fieldMap".equals(attribute.getName())) {
            Object[] values = (Object[])attribute.getValue();
            if (values != null && values.length % 2 == 1) {
                throw new IllegalArgumentException(
                    "@Metric fieldMap must have odd items representing fieldName -> field pairs");
            }
            if (values != null) {
                for (int i = 0; i < values.length; i += 2) {
                    fieldMap.put((String)values[i], (String)values[i + 1]);
                }
            }
        } else if ("sampling".equals(attribute.getName())) {
            sampling = (String)attribute.getValue();
        } else if ("source".equals(attribute.getName())) {
            source = (String)attribute.getValue();
        }
    }

    @Override
    public io.etrace.common.message.metric.Metric process(MapEventBean event) {
        Map<String, Object> props = event.getProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }

        Map<String, Field> map = newHashMap();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            Object obj = props.get(field);
            if (obj instanceof Map) {
                Map fieldsObj = (Map)obj;
                for (Object entryObj : fieldsObj.entrySet()) {
                    if (entryObj instanceof Map.Entry) {
                        Map.Entry entry = (Map.Entry)entryObj;
                        if (entry.getKey() instanceof String && entry.getValue() instanceof Field) {
                            // must create new field, if multi metrics use the same field, field merge() will cause
                            // problem
                            map.put((String)entry.getKey(), newField((Field)entry.getValue()));
                        }
                    }
                }
            } else if (obj instanceof Field) {
                // create new field
                map.put(field, newField((Field)obj));
            }
        }

        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String field = entry.getValue();
            Object obj = props.get(field);
            if (obj instanceof Field) {
                // create new field
                map.put(entry.getKey(), newField((Field)obj));
            }
        }

        if (map.isEmpty()) {
            return null;
        }

        io.etrace.common.message.metric.Metric metric = new io.etrace.common.message.metric.Metric();
        metric.setMetricName(getMetricName(props));
        metric.setMetricType(MetricType.Metric);
        metric.setTags(getTags(props));
        metric.setTimestamp(getTimestamp(props));
        metric.setFields(map);

        if (!Strings.isNullOrEmpty(sampling)) {
            Pair<MetricType, String> samplingMsg = getSamplingMsg(props, sampling);
            if (samplingMsg != null && samplingMsg.getKey() != null && samplingMsg.getValue() != null) {
                metric.setMetricType(samplingMsg.getKey());
                metric.setSampling(samplingMsg.getValue());
            }
        }

        if (!Strings.isNullOrEmpty(source)) {
            String metricSource = (String)props.get(source);
            if (metricSource != null) {
                metric.setSource(metricSource);
            }
        }

        return metric;
    }

    private Field newField(Field field) {
        return new Field(field.getAggregateType(), field.getValue());
    }

    @Override
    public void validation(Set<String> selectItems) {
        super.validation(selectItems);
        if ((fields.isEmpty() && fieldMap.isEmpty())) {
            throw new EsperConfigException("@Metric both fields and fieldMap are empty");
        }

        for (String field : fields) {
            if (!selectItems.contains(field)) {
                throw new EsperConfigException("@Metric field <" + field + "> not in select clause:" + selectItems);
            }
        }

        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            if (!selectItems.contains(entry.getValue())) {
                throw new EsperConfigException(
                    "@Metric fieldMap item <" + entry.getKey() + ":" + entry.getValue() + "> not in select clause:"
                        + selectItems);
            }
        }
    }

}
