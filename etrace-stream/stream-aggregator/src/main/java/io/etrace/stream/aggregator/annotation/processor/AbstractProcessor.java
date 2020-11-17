package io.etrace.stream.aggregator.annotation.processor;

import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import io.etrace.common.exception.EsperConfigException;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.Pair;
import io.etrace.stream.aggregator.EPEngine;
import io.etrace.stream.aggregator.annotation.AnnotationProcessor;
import io.etrace.stream.core.util.ObjectUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public abstract class AbstractProcessor implements AnnotationProcessor {
    protected String name;
    protected String[] tagNames;
    protected Set<String> nameParams;

    protected abstract void init(AnnotationAttribute attribute);

    @Override
    public void init(AnnotationPart annotation, EPStatementObjectModel model) {
        List<AnnotationAttribute> attributes = annotation.getAttributes();
        if (attributes == null) {
            return;
        }
        for (AnnotationAttribute attribute : attributes) {
            String attributeName = attribute.getName();
            if ("name".equals(attributeName)) {
                name = (String)attribute.getValue();
                Matcher m = EPEngine.PARAM_PATTERN.matcher(name);

                while (m.find()) {
                    if (nameParams == null) {
                        nameParams = newHashSet();
                    }
                    nameParams.add(m.group(1));
                }

            } else if ("tags".equals(attributeName)) {
                Object[] values = (Object[])attribute.getValue();
                if (values != null && values.length > 0) {
                    tagNames = new String[values.length];
                    for (int i = 0; i < tagNames.length; i++) {
                        tagNames[i] = (String)values[i];
                    }
                }
            } else {
                init(attribute);
            }
        }
    }

    String getMetricName(Map<String, Object> props) {
        String result = name;
        if (nameParams == null || nameParams.isEmpty()) {
            return result;
        }
        for (String param : nameParams) {
            Object paramObj = props.get(param);
            String paramStr = param;
            if (paramObj != null) {
                paramStr = ObjectUtil.toString(paramObj);
            }
            result = result.replace("{" + param + "}", paramStr);
        }
        return result;
    }

    Pair<MetricType, String> getSamplingMsg(Map<String, Object> props, String samplingKey) {
        if (samplingKey == null) {
            return null;
        }
        Object msg = props.get(samplingKey);
        if (msg == null) {
            return null;
        }
        return (Pair<MetricType, String>)msg;
    }

    // to be removed
    String getSamplingMsgv1(Map<String, Object> props, String samplingKey) {
        if (samplingKey == null) {
            return null;
        }
        Object msg = props.get(samplingKey);
        if (msg == null) {
            return null;
        }
        return ObjectUtil.toString(msg);
    }

    boolean hasTag() {
        return tagNames != null && tagNames.length > 0;
    }

    long getTimestamp(Map<String, Object> props) {
        Object time = props.get(EPEngine.TIMESTAMP);
        if (time == null) {
            time = props.get(EPEngine.TIME_MINUTES);
        }
        long timestamp;
        //get timestamp from result event
        if (time == null) {
            timestamp = System.currentTimeMillis();
        } else {
            timestamp = ObjectUtil.toLong(time);
        }
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    Map<String, String> getTags(Map<String, Object> props) {
        if (!hasTag()) {
            return Collections.emptyMap();
        }
        Map<String, String> tags = newHashMap();
        for (String tagName : tagNames) {
            Object paramObj = props.get(tagName);
            if (paramObj != null) {
                if (paramObj instanceof Map) {
                    Map mapTag = (Map)paramObj;
                    for (Object obj : mapTag.entrySet()) {
                        if (obj instanceof Map.Entry) {
                            Map.Entry entry = (Map.Entry)obj;
                            if (entry.getKey() != null
                                && entry.getValue() != null
                                && entry.getKey() instanceof String
                                && entry.getValue() instanceof String) {
                                tags.put((String)entry.getKey(), (String)entry.getValue());
                            }
                        }
                    }
                } else {
                    tags.put(tagName, ObjectUtil.toString(paramObj));
                }
            }
        }
        return tags;
    }

    @Override
    public void validation(Set<String> selectItems) {
        if (tagNames != null) {
            for (String tagName : tagNames) {
                if (!selectItems.contains(tagName)) {
                    throw new EsperConfigException("@Metric tag<" + tagName + "> not in select clause " + selectItems);
                }
            }
        }

        if (nameParams != null) {
            for (String nameParam : nameParams) {
                if (!selectItems.contains(nameParam)) {
                    throw new EsperConfigException(
                        "@Metric name param<" + nameParam + "> not in select clause " + selectItems);
                }
            }
        }
    }
}