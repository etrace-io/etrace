package io.etrace.stream.core.util;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.Field;

import java.util.Map;
import java.util.Objects;

import static io.etrace.common.constant.FieldName.*;

public class MetricUtil {

    public static void merge(Metric source, Metric other) {
        // merge sampling before fields
        mergeSampling(source, other);
        mergeFields(source.getFields(), other.getFields());
    }

    // 与FieldsAggregator不同 不能替换
    private static void mergeFields(Map<String, Field> fields, Map<String, Field> otherFields) {
        for (Map.Entry<String, Field> entry : otherFields.entrySet()) {
            if (fields.containsKey(entry.getKey())) {
                fields.get(entry.getKey()).merge(entry.getValue());
            } else {
                fields.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void mergeSampling(Metric source, Metric other) {
        if (Objects.isNull(other.getSampling()) || Objects.isNull(other.getMetricType())) {
            return;
        }

        if (Objects.isNull(source.getSampling()) || Objects.isNull(source.getMetricType())) {
            source.setSampling(other.getSampling());
            source.setMetricType(other.getMetricType());
            return;
        }

        switch (source.getMetricType()) {
            case Timer:
                if (!maxSampling(source, other, TIMER_MAX)) {
                    maxSampling(source, other, HISTOGRAM_MAX);
                }
                break;
            case Payload:
                maxSampling(source, other, PAYLOAD_MAX);
                break;
            case Histogram:
                maxSampling(source, other, HISTOGRAM_MAX);
                break;
            default:
        }
    }

    /**
     * 参考 MetricSamplingAggregator 兼容timer histogram采样处理
     *
     * @return whether both source and other have fieldName Field
     */
    private static boolean maxSampling(Metric source, Metric other, String fieldName) {
        Field sourceField = source.getField(fieldName);
        Field otherField = other.getField(fieldName);
        if (sourceField == null || otherField == null) {
            return false;
        }
        if (sourceField.getValue() < otherField.getValue()) {
            source.setSampling(other.getSampling());
        }
        return true;
    }

    public static int hash(Metric metric) {
        return Objects.hash(metric.getMetricName(), metric.getTags(), metric.getTimestamp(), metric.getSource());
    }
}
