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
package io.etrace.common.message.metric.codec;

import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.MetricMessageV1;
import io.etrace.common.message.metric.util.CodecUtil;

import java.io.*;

/**
 * 用于 etrace-consumer, shaka, watchdog 中解析 etrace-collector写入到kafka的 多filed metrics数据
 * <p>
 * 可兼容 etrace老版本
 */
public class FramedMetricMessageCodecV1 {

    private final static MetricHeaderCodecV1 metricHeaderCodec = new MetricHeaderCodecV1();
    private final static MetricHeaderCodecLegacyVersion metricHeaderCodecLegacy = new MetricHeaderCodecLegacyVersion();
    private final static MetricsCodec metricsCodecLegacy = new MetricsCodec();
    private final static MetricCodecV1 metricsCodecV1 = new MetricCodecV1();

    /**
     * 供etrace内部使用：兼容新老数据
     *
     * @param msg msg
     * @return {@link MetricMessage}
     * @throws IOException IOException
     */
    public MetricMessage decodeToLegacyVersion(byte[] msg) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(msg));
        MetricMessage metricMessage = new MetricMessage();
        byte[] headerData = CodecUtil.readLengthPrefixData(in);

        if (headerData != null) {
            metricMessage.setMetricHeader(metricHeaderCodec.decodeToLegacyVersion(headerData));
        }
        byte[] metricsData = CodecUtil.readLengthPrefixData(in);
        if (metricsData != null) {
            metricMessage.setMetrics(metricsCodecLegacy.decode(metricsData));
        }
        return metricMessage;
    }

    public MetricMessageV1 decode(byte[] msg) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(msg));
        MetricMessageV1 metricMessage = new MetricMessageV1();
        byte[] headerData = CodecUtil.readLengthPrefixData(in);
        if (headerData != null) {
            metricMessage.setMetricHeader(metricHeaderCodec.decode(headerData));
        }
        byte[] metricsData = CodecUtil.readLengthPrefixData(in);
        if (metricsData != null) {
            metricMessage.setMetrics(metricsCodecV1.decode(metricsData));
        }
        return metricMessage;
    }

    /**
     * 供etrace内部使用：写成老格式的数据
     *
     * @param msg msg
     * @return {@link byte[]}
     * @throws IOException IOException
     */
    public byte[] encodeToLegacyVersion(MetricMessage msg) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
        byte[] headerData = metricHeaderCodecLegacy.encode(msg.getMetricHeader());
        byte[] metricsData = metricsCodecLegacy.encode(msg.getMetrics());
        CodecUtil.writeLengthPrefixData(out, headerData);
        CodecUtil.writeLengthPrefixData(out, metricsData);
        out.flush();
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] encode(MetricMessageV1 msg) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
        byte[] headerData = metricHeaderCodec.encode(msg.getMetricHeader());
        byte[] metricsData = metricsCodecV1.encode(msg.getMetrics());
        CodecUtil.writeLengthPrefixData(out, headerData);
        CodecUtil.writeLengthPrefixData(out, metricsData);
        out.flush();
        return byteArrayOutputStream.toByteArray();
    }

}
