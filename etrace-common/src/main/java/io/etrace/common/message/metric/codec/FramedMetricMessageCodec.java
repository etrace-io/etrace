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

import io.etrace.common.io.MessageCodec;
import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.util.CodecUtil;

import java.io.*;

/**
 * 用于 etrace-consumer, shaka, watchdog 中解析 etrace-collector写入到kafka的 多filed metrics数据
 */
@Deprecated
public class FramedMetricMessageCodec implements MessageCodec<MetricMessage> {

    private final static MetricHeaderCodecLegacyVersion metricHeaderCodec = new MetricHeaderCodecLegacyVersion();
    private final static MetricsCodec metricsCodec = new MetricsCodec();

    @Override
    public MetricMessage decode(byte[] msg) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(msg));
        MetricMessage metricMessage = new MetricMessage();
        byte[] headerData = CodecUtil.readLengthPrefixData(in);
        if (headerData != null) {
            metricMessage.setMetricHeader(metricHeaderCodec.decode(headerData));
        }
        byte[] metricsData = CodecUtil.readLengthPrefixData(in);
        if (metricsData != null) {
            metricMessage.setMetrics(metricsCodec.decode(metricsData));
        }
        return metricMessage;
    }

    @Override
    public byte[] encode(MetricMessage obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
        byte[] headerData = metricHeaderCodec.encode(obj.getMetricHeader());
        byte[] metricsData = metricsCodec.encode(obj.getMetrics());
        CodecUtil.writeLengthPrefixData(out, headerData);
        CodecUtil.writeLengthPrefixData(out, metricsData);
        out.flush();
        return byteArrayOutputStream.toByteArray();
    }

}
