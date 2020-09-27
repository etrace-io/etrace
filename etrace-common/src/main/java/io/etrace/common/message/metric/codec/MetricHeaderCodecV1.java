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
import com.google.common.collect.Maps;
import io.etrace.common.message.metric.MetricHeader;
import io.etrace.common.message.metric.MetricHeaderV1;
import io.etrace.common.message.trace.codec.JSONCodecV1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 由 MetricQueue 去写的数据 兼容 新老的格式
 */
public class MetricHeaderCodecV1 {

    private static final JsonFactory factory = new JsonFactory();
    private static final int ESTIMATED_SIZE = 128;

    public MetricHeaderV1 decode(byte[] msg) throws IOException {
        if (msg == null) {
            return null;
        }
        MetricHeaderV1 header = new MetricHeaderV1();
        JsonParser parser = factory.createParser(msg);
        JsonToken token = parser.nextToken();
        if (token != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("Bad json data");
        }
        token = parser.nextToken();

        String version = parser.getText();
        if (version.startsWith(JSONCodecV1.METRIC_PREFIX_V1)) {
            byte index = 0;
            // only read 5 fields
            while (index <= 4 && token != null) {
                if (token != JsonToken.VALUE_NULL) {
                    switch (index) {
                        case 0:
                            header.setTenant(parser.getText());
                            break;
                        case 1:
                            header.setAppId(parser.getText());
                            break;
                        case 2:
                            header.setHostIp(parser.getText());
                            break;
                        case 3:
                            header.setHostName(parser.getText());
                            break;
                        case 4:
                            Map<String, String> extraProperties = JSONCodecV1.decodeExtraProperties(parser);
                            header.setExtraProperties(extraProperties);
                            break;
                        default:
                            throw new IllegalArgumentException("Bad json data: invalid index over 4");
                    }
                }
                token = parser.nextToken();
                index++;
            }
        } else {
            Map<String, String> extraProperties = Maps.newHashMap();

            byte index = 0;
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token != JsonToken.VALUE_NULL) {
                    switch (index) {
                        case 0:
                            // ignore legacy 'Topic' part
                            break;
                        case 1:
                            header.setAppId(parser.getText());
                            break;
                        case 2:
                            header.setHostIp(parser.getText());
                            break;
                        case 3:
                            header.setHostName(parser.getText());
                            break;
                        case 4:
                            extraProperties.put("cluster", parser.getText());
                            break;
                        case 5:
                            extraProperties.put("ezone", parser.getText());
                            break;
                        case 6:
                            extraProperties.put("idc", parser.getText());
                            break;
                        case 7:
                            extraProperties.put("mesosTaskId", parser.getText());
                            break;
                        case 8:
                            extraProperties.put("eleapposLabel", parser.getText());
                            break;
                        case 9:
                            extraProperties.put("eleapposSlaveFqdn", parser.getText());
                            break;
                    }
                }
                token = parser.nextToken();
                index++;
            }
            header.setExtraProperties(extraProperties);
        }
        return header;
    }

    public MetricHeader decodeToLegacyVersion(byte[] msg) throws IOException {
        if (msg == null) {
            return null;
        }
        MetricHeader header = new MetricHeader();
        JsonParser parser = factory.createParser(msg);
        JsonToken token = parser.nextToken();
        if (token != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("Bad json data");
        }
        token = parser.nextToken();
        String version = parser.getText();
        if (version.startsWith(JSONCodecV1.METRIC_PREFIX_V1)) {
            byte index = 0;
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token != JsonToken.VALUE_NULL) {
                    switch (index) {
                        case 0:
                            // write 'tenant' to 'topic'
                            header.setTopic(parser.getText());
                            break;
                        case 1:
                            header.setAppId(parser.getText());
                            break;
                        case 2:
                            header.setHostIp(parser.getText());
                            break;
                        case 3:
                            header.setHostName(parser.getText());
                            break;
                        case 4:
                            Map<String, String> extraProperties = JSONCodecV1.decodeExtraProperties(parser);

                            header.setCluster(extraProperties.get("cluster"));
                            header.setEzone(extraProperties.get("ezone"));
                            header.setIdc(extraProperties.get("idc"));
                            header.setMesosTaskId(extraProperties.get("mesosTaskId"));
                            header.setEleapposLabel(extraProperties.get("eleapposLabel"));
                            header.setEleapposSlaveFqdn(extraProperties.get("eleapposSlaveFqdn"));
                            break;
                    }
                }
                token = parser.nextToken();
                index++;
            }
        } else {
            byte index = 0;
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token != JsonToken.VALUE_NULL) {
                    switch (index) {
                        case 0:
                            header.setTopic(parser.getText());
                            break;
                        case 1:
                            header.setAppId(parser.getText());
                            break;
                        case 2:
                            header.setHostIp(parser.getText());
                            break;
                        case 3:
                            header.setHostName(parser.getText());
                            break;
                        case 4:
                            header.setCluster(parser.getText());
                            break;
                        case 5:
                            header.setEzone(parser.getText());
                            break;
                        case 6:
                            header.setIdc(parser.getText());
                            break;
                        case 7:
                            header.setMesosTaskId(parser.getText());
                            break;
                        case 8:
                            header.setEleapposLabel(parser.getText());
                            break;
                        case 9:
                            header.setEleapposSlaveFqdn(parser.getText());
                            break;
                    }
                }
                token = parser.nextToken();
                index++;
            }
        }
        return header;
    }

    public byte[] encode(MetricHeaderV1 header) throws IOException {
        if (header == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(ESTIMATED_SIZE);
        JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8);
        generator.writeStartArray();
        generator.writeString(JSONCodecV1.METRIC_PREFIX_V1);
        generator.writeString(header.getTenant());
        generator.writeString(header.getAppId());
        generator.writeString(header.getHostIp());
        generator.writeString(header.getHostName());
        generator.writeObject(header.getExtraProperties());
        generator.writeEndArray();
        generator.flush();
        return out.toByteArray();
    }
}
