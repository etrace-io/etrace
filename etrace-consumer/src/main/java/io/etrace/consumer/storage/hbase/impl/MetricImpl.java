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

package io.etrace.consumer.storage.hbase.impl;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.MetricFieldName;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.JSONUtil;
import io.etrace.consumer.storage.hbase.MetricTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class MetricImpl extends MetricTable {

    /**
     * shard(2) + metricType(1) + time(8)
     */
    private final static int MAGIC_SIZE = Bytes.SIZEOF_SHORT + Bytes.SIZEOF_BYTE + Bytes.SIZEOF_LONG;

    private final static String SAMPLING_MAX = "max";
    private final static String SAMPLING_VALUE = "value";

    @Override
    public byte[] buildRowKey(short shard, Metric metric) {
        MetricType metricType = metric.getMetricType();
        if (null == metricType) {
            return null;
        }
        long timestamp = metric.getTimestamp();
        Map<String, String> tags = metric.getTags();

        byte[] metricsKeyData = metric.getMetricName().getBytes();
        int tagSize = tags == null ? 0 : tags.size();

        int len = MAGIC_SIZE + metricsKeyData.length + tagSize * 8;
        Long maxValue = getMaxField(metric);
        if (null != maxValue) {
            len += Bytes.SIZEOF_LONG;
        }

        byte[] data = new byte[len];
        int offset = Bytes.putShort(data, 0, shard);
        offset = Bytes.putByte(data, offset, metricType.code());
        offset = Bytes.putBytes(data, offset, metricsKeyData, 0, metricsKeyData.length);
        offset = Bytes.putLong(data, offset, timestamp);
        if (null != maxValue) {
            offset = Bytes.putLong(data, offset, Long.MAX_VALUE - maxValue);
        }

        if (null != tags) {
            Map<Integer, Integer> tagHashTreeMap = new TreeMap<>();
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                tagHashTreeMap.put(entry.getKey().hashCode(), entry.getValue().hashCode());
            }
            for (Map.Entry<Integer, Integer> entry : tagHashTreeMap.entrySet()) {
                offset = Bytes.putInt(data, offset, entry.getKey());
                offset = Bytes.putInt(data, offset, entry.getValue());
            }
        }
        return data;
    }

    @Override
    public byte[] buildQualifierValue(Metric metric) throws IOException {
        Map<String, String> samplingMap = new HashMap<>();
        MetricType metricType = metric.getMetricType();
        if (metricType == null) {
            return null;
        }
        // ratio,metric type not need to sampling for now
        if (MetricType.Ratio.equals(metricType) || MetricType.Metric.equals(metricType)) {
            return null;
        }
        if (hasMaxField(metric.getMetricType())) {
            samplingMap.put(SAMPLING_MAX, metric.getSampling());
        } else {
            samplingMap.put(SAMPLING_VALUE, metric.getSampling());
        }
        return JSONUtil.toJsonAsBytes(samplingMap);
    }

    public boolean hasMaxField(MetricType metricType) {
        return (metricType.equals(MetricType.Timer)
            || metricType.equals(MetricType.Payload)
            || metricType.equals(MetricType.Histogram));
    }

    private Long getMaxField(Metric metric) {
        if (!hasMaxField(metric.getMetricType())) {
            return null;
        }
        Long maxValue;
        switch (metric.getMetricType()) {
            case Timer:
                Map<String, Field> fields = metric.getFields();
                Field field = fields.get(MetricFieldName.TIMER_MAX);
                if (field == null) {
                    field = fields.get(MetricFieldName.HISTOGRAM_MAX);
                }
                maxValue = (long)field.getValue();
                break;
            case Histogram:
                maxValue = (long)metric.getField(MetricFieldName.HISTOGRAM_MAX).getValue();
                break;
            case Payload:
                maxValue = (long)metric.getField(MetricFieldName.PAYLOAD_MAX).getValue();
                break;
            default:
                maxValue = null;
        }
        return maxValue;
    }
}
