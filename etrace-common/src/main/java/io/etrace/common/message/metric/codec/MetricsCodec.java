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

import com.fasterxml.jackson.core.*;
import io.etrace.common.io.MessageCodec;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.message.metric.util.TransferUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
public class MetricsCodec implements MessageCodec<List<Metric>> {

    private static final JsonFactory factory = new JsonFactory();
    private static final int ESTIMATED_SIZE = 128;

    @Override
    public List<Metric> decode(byte[] msg) throws IOException {
        if (msg == null) {
            return null;
        }
        JsonParser parser = factory.createParser(msg);
        List<Metric> metrics = new ArrayList<>();
        decodeMetric(parser, metrics);
        return metrics;
    }

    private void decodeMetric(JsonParser parser, List<Metric> metrics) throws IOException {
        JsonToken token = parser.nextToken();
        TransferUtils.ensureStartArrayToken(token);
        token = parser.nextToken();
        while (token != JsonToken.END_ARRAY && token != null) {
            int index = 0;
            Metric metric = new Metric();
            metric.setMetricType(MetricType.Metric);
            token = parser.nextToken();
            while (token != null && token != JsonToken.END_ARRAY) {
                switch (index) {
                    case 0:
                        if (token == JsonToken.VALUE_NULL) {
                            metric.setMetricType(null);
                        } else {
                            metric.setMetricType(MetricType.fromIdentifier(parser.getText()));
                        }
                        break;
                    case 1:
                        metric.setMetricName(parser.getText());
                        break;
                    case 2:
                        metric.setTimestamp(parser.getLongValue());
                        break;
                    case 3:
                        if (token == JsonToken.VALUE_NULL) {
                            break;
                        }
                        metric.setSampling(parser.getText());
                        break;
                    case 4:
                        if (token == JsonToken.VALUE_NULL) {
                            break;
                        }
                        metric.setSource(parser.getText());
                        break;
                    case 5:
                        Map<String, String> tags = TransferUtils.parseTags(parser);
                        metric.setTags(tags);
                        break;
                    case 6:
                        metric.setFields(TransferUtils.parseFields(parser));
                        break;
                }
                token = parser.nextToken();
                index++;
            }
            token = parser.nextToken();
            metrics.add(metric);
        }
    }

    @Override
    public byte[] encode(List<Metric> metrics) throws IOException {
        if (metrics == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(ESTIMATED_SIZE);
        JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8);
        generator.writeStartArray();
        for (Metric metric : metrics) {
            encodeMetric(generator, metric);
        }
        generator.writeEndArray();
        generator.flush();
        return out.toByteArray();
    }

    private void encodeMetric(JsonGenerator generator, Metric metric) throws IOException {
        generator.writeStartArray();
        if (metric.getMetricType() == null) {
            generator.writeNull();
        } else {
            generator.writeString(metric.getMetricType().toIdentifier());
        }
        generator.writeString(metric.getMetricName());
        generator.writeNumber(metric.getTimestamp());

        if (metric.getSampling() != null) {
            generator.writeString(metric.getSampling());
        } else {
            generator.writeNull();
        }

        if (metric.getSource() != null) {
            generator.writeString(metric.getSource());
        } else {
            generator.writeNull();
        }

        Map<String, String> tags = metric.getTags();
        if (tags == null) {
            generator.writeNull();
        } else {
            generator.writeStartObject();
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();
        }

        Map<String, Field> fields = metric.getFields();
        if (fields == null) {
            generator.writeNull();
        } else {
            generator.writeStartArray();
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                generator.writeStartArray();
                generator.writeString(entry.getKey());
                generator.writeString(entry.getValue().getAggregateType().name());
                generator.writeNumber(entry.getValue().getValue());
                generator.writeEndArray();
            }
            generator.writeEndArray();
        }
        generator.writeEndArray();
    }
}
