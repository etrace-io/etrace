/*
 * Copyright 2020 etrace.io
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.etrace.common.message.metric.*;
import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.message.metric.util.TransferUtils;
import io.etrace.common.message.trace.codec.JSONCodecV1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用于 collector解析etrace-agent中写入的metrics数据格式 替代 metric-common中的 MetricV1MessageDecoder 包括 encode/decode metrics
 */
public class MetricCodecV1 {

    private final static JsonFactory factory = new JsonFactory();

    private MetricsCodec metricsCodec = new MetricsCodec();

    /**
     * 因为Metric ecode的地方未更改，因此直接复用老版本的
     *
     * @param metrics metrics
     * @return {@link byte[]}
     * @throws IOException IOException
     */
    public byte[] encode(List<Metric> metrics) throws IOException {
        return metricsCodec.encode(metrics);
    }

    /**
     * 因为Metric decode的地方未更改，因此直接复用老版本的
     *
     * @param msg msg
     * @return {@link List}* @throws IOException IOException
     */
    public List<Metric> decode(byte[] msg) throws IOException {
        return metricsCodec.decode(msg);
    }

    public List<MetricMessageV1> decodeFromV1(byte[] bytes) throws IOException {
        try (JsonParser parser = factory.createParser(bytes)) {
            return decodeToMetricMessageV1(parser);
        }
    }

    private List<MetricMessageV1> decodeToMetricMessageV1(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        TransferUtils.ensureStartArrayToken(token);
        //move to first field
        token = parser.nextToken();
        if (token == JsonToken.END_ARRAY) {
            return Collections.emptyList();
        }
        List<MetricMessageV1> metricMessageList = new ArrayList<>();

        while (token != JsonToken.END_ARRAY && token != null) {
            MetricMessageV1 metricMessage = new MetricMessageV1();
            MetricHeaderV1 metricHeader = new MetricHeaderV1();
            List<Metric> metrics = new ArrayList<>();

            TransferUtils.ensureStartArrayToken(token);
            token = parser.nextToken();

            String version = parser.getText();
            if (version.startsWith(JSONCodecV1.METRIC_PREFIX_V1)) {
                byte index = 0;
                while (token != JsonToken.END_ARRAY && token != null) {
                    if (token != JsonToken.VALUE_NULL) {
                        switch (index) {
                            case 0:
                                // write 'tenant'
                                metricHeader.setTenant(parser.getText());
                                break;
                            case 1:
                                metricHeader.setAppId(parser.getText());
                                break;
                            case 2:
                                metricHeader.setHostIp(parser.getText());
                                break;
                            case 3:
                                metricHeader.setHostName(parser.getText());
                                break;
                            case 4:
                                TransferUtils.ensureStartArrayToken(token);
                                decodeToV2(parser, metrics);
                                break;
                            case 5:
                                Map<String, String> extraProperties = JSONCodecV1.decodeExtraProperties(parser);
                                metricHeader.setExtraProperties(extraProperties);

                                break;
                            default:
                                throw new IllegalArgumentException("Bad json data: invalid index over 5");
                        }
                    }
                    token = parser.nextToken();
                    index++;
                }
                metricMessage.setMetricHeader(metricHeader);
                metricMessage.setMetrics(metrics);
                metricMessageList.add(metricMessage);
                //move to next value
                token = parser.nextToken();
            } else {
                throw new IllegalArgumentException("Bad json data: invalid prefix version [" + parser.getText() + "]");
            }
        }
        return metricMessageList;
    }

    public List<MetricMessage> decodeToMetricMessageLegacyVersion(byte[] bytes) throws IOException {
        try (JsonParser parser = factory.createParser(bytes)) {
            return decodeToMetricMessageLegacyVersion(parser);
        }
    }

    private List<MetricMessage> decodeToMetricMessageLegacyVersion(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        TransferUtils.ensureStartArrayToken(token);
        //move to first field
        token = parser.nextToken();
        List<MetricMessage> metricMessageList = new ArrayList<>();
        if (token == JsonToken.END_ARRAY) {
            return metricMessageList;
        }
        while (token != JsonToken.END_ARRAY && token != null) {
            MetricMessage metricMessage = new MetricMessage();
            MetricHeader metricHeader = new MetricHeader();
            List<Metric> metrics = new ArrayList<>();

            TransferUtils.ensureStartArrayToken(token);
            token = parser.nextToken();

            String version = parser.getText();
            if (version.startsWith(JSONCodecV1.METRIC_PREFIX_V1)) {
                // skip first METRIC_PREFIX_V1 mark
                parser.nextToken();

                byte index = 0;
                while (token != JsonToken.END_ARRAY && token != null) {
                    if (token != JsonToken.VALUE_NULL) {
                        switch (index) {
                            case 0:
                                // write 'tenant' to 'topic'
                                metricHeader.setTopic(parser.getText());
                                break;
                            case 1:
                                metricHeader.setAppId(parser.getText());
                                break;
                            case 2:
                                metricHeader.setHostIp(parser.getText());
                                break;
                            case 3:
                                metricHeader.setHostName(parser.getText());
                                break;
                            case 4:
                                Map<String, String> extraProperties = JSONCodecV1.decodeExtraProperties(parser);

                                metricHeader.setCluster(extraProperties.get("cluster"));
                                metricHeader.setEzone(extraProperties.get("ezone"));
                                metricHeader.setIdc(extraProperties.get("idc"));
                                metricHeader.setMesosTaskId(extraProperties.get("mesosTaskId"));
                                metricHeader.setEleapposLabel(extraProperties.get("eleapposLabel"));
                                metricHeader.setEleapposSlaveFqdn(extraProperties.get("eleapposSlaveFqdn"));
                                break;
                            case 5:
                                TransferUtils.ensureStartArrayToken(token);
                                decodeToV2(parser, metrics);
                                break;
                            default:
                                throw new IllegalArgumentException("Bad json data: invalid index over 5");
                        }
                    }
                    token = parser.nextToken();
                    index++;
                }
                metricMessage.setMetricHeader(metricHeader);
                metricMessage.setMetrics(metrics);
                metricMessageList.add(metricMessage);
                //move to next value
                token = parser.nextToken();
            } else {
                byte index = 0;
                while (token != JsonToken.END_ARRAY && token != null) {
                    if (token != JsonToken.VALUE_NULL) {
                        switch (index) {
                            case 0:
                                metricHeader.setTopic(parser.getText());
                                break;
                            case 1:
                                metricHeader.setAppId(parser.getText());
                                break;
                            case 2:
                                metricHeader.setHostIp(parser.getText());
                                break;
                            case 3:
                                metricHeader.setHostName(parser.getText());
                                break;
                            case 4:
                                metricHeader.setCluster(parser.getText());
                                break;
                            case 5:
                                metricHeader.setEzone(parser.getText());
                                break;
                            case 6:
                                metricHeader.setIdc(parser.getText());
                                break;
                            case 7:
                                metricHeader.setMesosTaskId(parser.getText());
                                break;
                            case 8:
                                metricHeader.setEleapposLabel(parser.getText());
                                break;
                            case 9:
                                metricHeader.setEleapposSlaveFqdn(parser.getText());
                                break;
                            case 10:
                                TransferUtils.ensureStartArrayToken(token);
                                decodeToV2(parser, metrics);
                                break;
                            default:
                                throw new IllegalArgumentException("Bad json data: invalid index over 10");
                        }
                    }
                    token = parser.nextToken();
                    index++;
                }
                metricMessage.setMetricHeader(metricHeader);
                metricMessage.setMetrics(metrics);
                metricMessageList.add(metricMessage);
                //move to next value
                token = parser.nextToken();
            }
        }
        return metricMessageList;
    }

    private void decodeToV2(JsonParser parser, List<Metric> metrics) throws IOException {
        JsonToken token = parser.nextToken();
        TransferUtils.ensureStartArrayToken(token);
        while (token != JsonToken.END_ARRAY && token != null) {
            token = parser.nextToken();
            while (token != null && token != JsonToken.END_ARRAY) {
                switch (parser.getText()) {
                    case MetricType.COUNTER:
                        metrics.add(decodeCounterToV2(parser));
                        break;
                    case MetricType.GAUGE:
                        metrics.add(decodeGaugeToV2(parser));
                        break;
                    case MetricType.TIMER:
                        metrics.add(decodeTimerToV2(parser));
                        break;
                    case MetricType.PAYLOAD:
                        metrics.add(decodePayloadToV2(parser));
                        break;
                    case MetricType.RATIO:
                        metrics.add(decodeRatioToV2(parser));
                        break;
                    case MetricType.HISTOGRAM:
                        metrics.add(decodeHistogramToV2(parser));
                        break;
                    case MetricType.METRIC:
                        metrics.add(decodeMetricToV2(parser));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown metric type: " + parser.getText());
                }
                token = parser.getCurrentToken();
            }
            token = parser.nextToken();
        }
    }

    private Metric decodeCounterToV2(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Counter);
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    metric.addField(MetricFieldName.COUNTER_COUNT, new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;
    }

    private Metric decodeGaugeToV2(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Gauge);
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    metric.addField(MetricFieldName.GAUGE_VALUE,
                        new Field(AggregateType.GAUGE, parser.getDoubleValue()));
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;
    }

    private Metric decodeTimerToV2(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Timer);
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    metric.addField(MetricFieldName.TIMER_SUM, new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 4:
                    metric.addField(MetricFieldName.TIMER_COUNT, new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 5:
                    metric.addField(MetricFieldName.TIMER_MIN, new Field(AggregateType.MIN, parser.getLongValue()));
                    break;
                case 6:
                    metric.addField(MetricFieldName.TIMER_MAX, new Field(AggregateType.MAX, parser.getLongValue()));
                    break;
                case 7:
                    // upper enable  ignore
                default:
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;
    }

    private Metric decodePayloadToV2(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Payload);
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    metric.addField(MetricFieldName.PAYLOAD_SUM, new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 4:
                    metric.addField(MetricFieldName.PAYLOAD_COUNT, new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 5:
                    metric.addField(MetricFieldName.PAYLOAD_MIN, new Field(AggregateType.MIN, parser.getLongValue()));
                    break;
                case 6:
                    metric.addField(MetricFieldName.PAYLOAD_MAX, new Field(AggregateType.MAX, parser.getLongValue()));
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;
    }

    /**
     * @param parser
     * @return
     * @throws IOException
     */
    private Metric decodeRatioToV2(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Ratio);
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    metric.addField(MetricFieldName.RATIO_NUMERATOR,
                        new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 4:
                    metric.addField(MetricFieldName.RATIO_DENOMINATOR,
                        new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;
    }

    private Metric decodeHistogramToV2(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Histogram);
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    // histogram baseNumber  ignore
                    break;
                case 4:
                    metric.addField(MetricFieldName.HISTOGRAM_MIN, new Field(AggregateType.MIN, parser.getLongValue()));
                    break;
                case 5:
                    metric.addField(MetricFieldName.HISTOGRAM_MAX, new Field(AggregateType.MAX, parser.getLongValue()));
                    break;
                case 6:
                    metric.addField(MetricFieldName.HISTOGRAM_SUM, new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 7:
                    metric.addField(MetricFieldName.HISTOGRAM_COUNT,
                        new Field(AggregateType.SUM, parser.getLongValue()));
                    break;
                case 8:
                    // distributionType  ignore
                    break;
                case 9:
                    if (token == JsonToken.START_ARRAY) {
                        int tmpIndex = 0;
                        token = parser.nextToken();
                        while (token != null && token != JsonToken.END_ARRAY) {
                            long value = parser.getLongValue();
                            if (value != 0) {
                                metric.addField(MetricFieldName.HISTOGRAM_FIELD_PREFIX + tmpIndex,
                                    new Field(AggregateType.SUM, parser.getLongValue()));
                            }
                            token = parser.nextToken();
                            tmpIndex++;
                        }
                    } else if (token == JsonToken.START_OBJECT) {
                        token = parser.nextToken();
                        while (token != null && token != JsonToken.END_OBJECT) {
                            String key = parser.getCurrentName();
                            token = parser.nextToken();
                            long value = parser.getLongValue();
                            if (value != 0) {
                                metric.addField(MetricFieldName.HISTOGRAM_FIELD_PREFIX + key,
                                    new Field(AggregateType.SUM, parser.getLongValue()));
                            }
                            token = parser.nextToken();
                        }
                    } else {
                        throw new IllegalArgumentException("Bad json data with bad histogram data");
                    }
                    break;
                default:
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;
    }

    /**
     * @param parser
     * @return
     * @throws IOException
     */
    private Metric decodeMetricToV2(JsonParser parser) throws IOException {
        int index = 0;
        Metric metric = new Metric();
        metric.setMetricType(MetricType.Metric);
        JsonToken token = parser.nextToken();
        while (token != null && token != JsonToken.END_ARRAY) {
            switch (index) {
                case 0:
                    metric.setMetricName(parser.getText());
                    break;
                case 1:
                    metric.setTimestamp(parser.getLongValue());
                    break;
                case 2:
                    Map<String, String> tags = TransferUtils.parseTags(parser);
                    metric.setTags(tags);
                    break;
                case 3:
                    metric.setFields(TransferUtils.parseFields(parser));
                    break;
            }
            token = parser.nextToken();
            index++;
        }
        return metric;

    }

}
