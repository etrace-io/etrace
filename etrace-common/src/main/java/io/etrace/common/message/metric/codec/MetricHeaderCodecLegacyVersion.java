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
import io.etrace.common.message.metric.MetricHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Deprecated
public class MetricHeaderCodecLegacyVersion implements MessageCodec<MetricHeader> {

    private static final JsonFactory factory = new JsonFactory();
    private static final int ESTIMATED_SIZE = 128;

    @Override
    public MetricHeader decode(byte[] msg) throws IOException {
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
        return header;
    }

    @Override
    public byte[] encode(MetricHeader header) throws IOException {
        if (header == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(ESTIMATED_SIZE);
        JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8);
        generator.writeStartArray();
        generator.writeString(header.getTopic());
        generator.writeString(header.getAppId());
        generator.writeString(header.getHostIp());
        generator.writeString(header.getHostName());
        generator.writeString(header.getCluster());
        generator.writeString(header.getEzone());
        generator.writeString(header.getIdc());
        generator.writeString(header.getMesosTaskId());
        generator.writeString(header.getEleapposLabel());
        generator.writeString(header.getEleapposSlaveFqdn());
        generator.writeEndArray();
        generator.flush();
        return out.toByteArray();
    }

}
