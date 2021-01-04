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

package io.etrace.common.message.trace.codec;

import com.fasterxml.jackson.core.*;
import com.google.common.collect.Maps;
import io.etrace.common.message.trace.*;
import io.etrace.common.message.trace.impl.EventImpl;
import io.etrace.common.message.trace.impl.HeartbeatImpl;
import io.etrace.common.message.trace.impl.TransactionImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * encode/decode callstack
 */
public class JSONCodecV1 {

    public static final String MESSAGE_PREFIX_V1 = "#v1";
    public static final String CALLSTACK_PREFIX_V1 = MESSAGE_PREFIX_V1 + "#t1";
    public static final String METRIC_PREFIX_V1 = MESSAGE_PREFIX_V1 + "#t2";

    private static JsonFactory jsonFactory = new JsonFactory();

    /**
     * 为Json编码 目前仅用于 Consumer controller 中以JSON序列化返回 查询到的数据 但是此处为每个Message额外添加了一个"_type"字段，目前前端会使用该字段。
     *
     * @param callStack 调用堆栈
     * @return {@link byte[]}
     * @throws IOException IOException
     */
    @Deprecated
    public static byte[] encodeAsJson(CallStackV1 callStack) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            encodeAsJson(callStack, generator);
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toByteArray();
    }

    protected static void encodeAsJson(CallStackV1 callStack, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("appId", callStack.getAppId());
        generator.writeStringField("hostIp", callStack.getHostIp());
        generator.writeStringField("hostName", callStack.getHostName());
        generator.writeStringField("requestId", callStack.getRequestId());
        generator.writeStringField("id", callStack.getId());
        generator.writeFieldName("message");

        encodeMessage(generator, callStack.getMessage());

        for (Map.Entry<String, String> entry : callStack.getExtraProperties().entrySet()) {
            generator.writeStringField(entry.getKey(), entry.getValue());
        }

        generator.writeEndObject();

        generator.flush();
    }

    private static void encodeMessage(JsonGenerator jg, Message message) throws IOException {
        jg.writeStartObject();
        if (message instanceof Event) {
            jg.writeStringField("_type", "event");
        } else if (message instanceof Transaction) {
            jg.writeStringField("_type", "transaction");
        } else if (message instanceof Heartbeat) {
            jg.writeStringField("_type", "heartbeat");
        }
        jg.writeStringField("type", message.getType());
        jg.writeStringField("name", message.getName());
        jg.writeNumberField("id", message.getId());
        jg.writeStringField("status", message.getStatus());
        jg.writeNumberField("timestamp", message.getTimestamp());
        jg.writeBooleanField("complete", message.isCompleted());
        if (message.getTags() != null && message.getTags().size() > 0) {
            jg.writeFieldName("tags");
            jg.writeStartObject();
            for (Map.Entry<String, String> entry : message.getTags().entrySet()) {
                jg.writeStringField(entry.getKey(), entry.getValue());
            }
            jg.writeEndObject();
        }
        if (message instanceof Event) {
            jg.writeStringField("data", ((Event)message).getData());
        } else if (message instanceof Transaction) {
            Transaction transaction = (Transaction)message;
            jg.writeNumberField("duration", transaction.getDuration());
            if (transaction.getChildren() != null && transaction.getChildren().size() > 0) {
                jg.writeFieldName("children");
                jg.writeStartArray();
                for (Message child : transaction.getChildren()) {
                    encodeMessage(jg, child);
                }
                jg.writeEndArray();
            }
        } else if (message instanceof Heartbeat) {
            jg.writeStringField("data", ((Heartbeat)message).getData());
        }
        jg.writeEndObject();
    }

    /**
     * 由数组格式编码Callstacks Json数组 将多个 callstack 以 array格式 序列化，并组合成一个 array。
     *
     * @param callStacks 调用堆栈
     * @return {@link byte[]}
     * @throws IOException IOException
     */
    public static byte[] encodeCallstacksByArrayFormat(List<CallStackV1> callStacks) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            generator.writeStartArray();
            for (CallStackV1 callStack : callStacks) {
                encodeCallstackByArrayFormat(callStack, generator);
            }
            generator.writeEndArray();
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toByteArray();
    }

    /**
     * 编码Callstack数组格式 将单个callstack 以 array格式 序列化
     *
     * @param callStack 调用堆栈
     * @return {@link byte[]}
     * @throws IOException IOException
     */
    public static byte[] encodeCallstackByArrayFormat(CallStackV1 callStack) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            encodeCallstackByArrayFormat(callStack, generator);
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toByteArray();
    }

    public static void encodeCallstackByArrayFormat(CallStackV1 callStack, JsonGenerator generator) throws IOException {
        generator.writeStartArray();

        generator.writeString(CALLSTACK_PREFIX_V1);

        generator.writeString(callStack.getAppId());
        generator.writeString(callStack.getHostIp());
        generator.writeString(callStack.getHostName());
        generator.writeString(callStack.getRequestId());
        generator.writeString(callStack.getId());

        AbstractMessageHelper.encodeMessageAsArray(callStack.getMessage(), generator);

        generator.writeStartObject();
        if (callStack.getExtraProperties() != null) {
            for (Map.Entry<String, String> entry : callStack.getExtraProperties().entrySet()) {
                generator.writeFieldName(entry.getKey());
                generator.writeString(entry.getValue());
            }
        }
        generator.writeEndObject();

        generator.writeEndArray();
        generator.flush();
    }

    /**
     * 解析 Array格式的 原始数据
     */
    public static CallStackV1 decodeToV1FromArrayFormat(byte[] data) throws IOException {
        JsonParser parser = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            parser = jsonFactory.createParser(input);
            return decodeToV1FromArrayFormat(parser);
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static CallStackV1 decodeToV1FromArrayFormat(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("Bad json data");
        }
        token = parser.nextToken();//move to first field

        if (token == JsonToken.END_ARRAY) {
            return null;
        }
        CallStackV1 callStack;
        if (parser.getText().startsWith(CALLSTACK_PREFIX_V1)) {
            callStack = new CallStackV1();
            // skip version
            parser.nextToken();

            byte index = 0;
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token != JsonToken.VALUE_NULL) {
                    switch (index) {
                        case 0:
                            callStack.setAppId(parser.getText());
                            break;
                        case 1:
                            callStack.setHostIp(parser.getText());
                            break;
                        case 2:
                            callStack.setHostName(parser.getText());
                            break;
                        case 3:
                            callStack.setRequestId(parser.getText());
                            break;
                        case 4:
                            callStack.setId(parser.getText());
                            break;
                        case 5:
                            if (token != JsonToken.START_ARRAY) {
                                throw new IllegalArgumentException("Bad json data with bad message data");
                            }
                            callStack.setMessage(decodeMessageFromArrayFormat(parser));
                            break;
                        case 6:
                            Map<String, String> extraProperties = decodeExtraProperties(parser);
                            callStack.setExtraProperties(extraProperties);
                            break;
                        default:
                            throw new IllegalArgumentException("Bad json data: invalid index of 7");
                    }
                }
                token = parser.nextToken();//move to next value
                index++;
            }
            return callStack;
        } else {
            throw new IllegalArgumentException("Bad json data: invalid prefix version [" + parser.getText() + "]");
        }
    }

    public static Map<String, String> decodeExtraProperties(JsonParser parser) throws IOException {
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            return Collections.emptyMap();
        } else {
            JsonToken token = parser.nextToken();
            Map<String, String> map = Maps.newHashMap();
            while (token != null && token != JsonToken.END_OBJECT) {
                String key = parser.getText();
                token = parser.nextToken();
                String value = parser.getText();
                token = parser.nextToken();
                map.put(key, value);
            }
            return map;
        }
    }

    /**
     * 解析消息的部分（在"#v1#t1","one_appId","127.0.0.1","my_hostname","requestId","id"之后的部分）
     *
     * @param parser parser
     * @return {@link Message}
     * @throws IOException IOException
     */
    public static Message decodeMessageFromArrayFormat(JsonParser parser) throws IOException {
        Message message = null;
        JsonToken token = parser.nextToken();//move first filed
        byte index = 0;
        while (token != null && token != JsonToken.END_ARRAY) {
            if (token != JsonToken.VALUE_NULL) {
                switch (index) {
                    case 0:
                        String type = parser.getText();
                        if ("event".equals(type)) {
                            message = new EventImpl();
                        } else if ("transaction".equals(type)) {
                            message = new TransactionImpl();
                        } else if ("heartbeat".equals(type)) {
                            message = new HeartbeatImpl();
                        }
                        break;
                    case 1:
                        if (message != null) {
                            message.setType(parser.getText());
                        }
                        break;
                    case 2:
                        if (message != null) {
                            message.setName(parser.getText());
                        }
                        break;
                    case 3:
                        if (message != null) {
                            message.setStatus(parser.getText());
                        }
                        break;
                    case 4:
                        if (message != null) {
                            message.setId(parser.getLongValue());
                        }
                        break;
                    case 5:
                        if (message != null) {
                            ((AbstractMessage)message).setTimestamp(parser.getLongValue());
                        }
                        break;
                    case 6:
                        if (message != null) {
                            AbstractMessageHelper.setCompleted(message, parser.getBooleanValue());
                        }
                        break;
                    case 7:
                        if (token == JsonToken.START_OBJECT) {
                            token = parser.nextToken();//move first tag key
                        }
                        Map<String, String> tags = new HashMap<>();
                        while (token != null && token != JsonToken.END_OBJECT) {
                            String key = parser.getCurrentName();
                            token = parser.nextToken();//move to value
                            if (message != null) {
                                tags.put(key, parser.getText());
                            }
                            token = parser.nextToken();//move to next key
                        }
                        if (message != null) {
                            AbstractMessageHelper.addTagsForJsonDecode(message, tags);
                        }
                        break;
                    case 8:
                        if (message != null) {
                            if (message instanceof Event) {
                                ((Event)message).setData(parser.getText());
                            } else if (message instanceof Heartbeat) {
                                ((Heartbeat)message).setData(parser.getText());
                            } else {
                                ((Transaction)message).setDuration(parser.getLongValue());
                            }
                        }
                        break;
                    case 9://only for transaction children
                        token = parser.nextToken();
                        while (token != null && token != JsonToken.END_ARRAY) {
                            if (message instanceof Transaction) {
                                ((Transaction)message).addChild(decodeMessageFromArrayFormat(parser));
                            }
                            token = parser.nextToken();//move to next message
                        }
                        break;
                }
            }
            token = parser.nextToken();//move to next value
            index++;
        }
        if (message != null) {
            if (message.getType() == null) {
                message.setType("Null");
            }
            if (message.getName() == null) {
                message.setName("Null");
            }
        }
        return message;
    }

    /**
     * 解析 List的Array格式的 原始数据
     */
    public static List<CallStackV1> decodeListArrayFormat(byte[] data) throws IOException {
        List<CallStackV1> callStacks;
        JsonParser parser = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            parser = jsonFactory.createJsonParser(input);
            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NULL) {
                return null;
            }
            callStacks = new ArrayList<>();

            if (token != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Bad json array data");
            }
            while (token != JsonToken.END_ARRAY && token != null) {
                callStacks.add(decodeToV1FromArrayFormat(parser));
                //move to next call stack
                token = parser.nextToken();
            }

            return callStacks;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    /**
     * 用于在 collector中将agent发送过来的 Callstack Array 解析成 List<byte[]>，再发送到kafka 这一步其实可以省略，直接将agent原始数据发送即可。但是，为了兼容性，先如此处理。
     * 待此版本完成替代老版本后，可移除此步骤（若移除，需要调整consumer/stream的处理逻辑）
     */
    @Deprecated
    public static List<byte[]> decodeAgentDataToList(byte[] data) throws IOException {
        List<byte[]> callStacks = new ArrayList<>();
        JsonParser parser = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            ByteArrayInputStream rawInput = new ByteArrayInputStream(data);
            parser = jsonFactory.createJsonParser(input);
            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NULL) {
                return Collections.emptyList();
            }
            if (token != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Bad json array data");
            }
            JsonLocation location = parser.getCurrentLocation();
            int offset = location.getColumnNr();
            rawInput.skip(offset - 1);
            token = parser.nextToken();//move to first object
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token == JsonToken.START_ARRAY) {
                    token = parser.nextToken();//move to first value

                    if (parser.getText().startsWith(CALLSTACK_PREFIX_V1)) {
                        // skip version
                        parser.nextToken();
                    }

                    byte index = 0;
                    while (token != JsonToken.END_ARRAY && token != null) {
                        if (token != JsonToken.VALUE_NULL) {
                            switch (index) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    if (token != JsonToken.START_ARRAY) {
                                        throw new IllegalArgumentException("Bad json data");
                                    }
                                    parseMessage(parser);
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    break;
                                case 8:
                                    break;
                            }
                        }
                        token = parser.nextToken();//move to next value
                        index++;
                    }
                }
                location = parser.getCurrentLocation();
                byte[] d = new byte[location.getColumnNr() - offset];
                rawInput.read(d);
                rawInput.skip(1);
                callStacks.add(d);
                offset = location.getColumnNr() + 1;
                token = parser.nextToken();//move to next call stack
            }
            return callStacks;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static void parseMessage(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();//move first filed
        byte index = 0;
        String type = null;
        String eventType = null;
        while (token != null && token != JsonToken.END_ARRAY) {
            if (token != JsonToken.VALUE_NULL) {
                switch (index) {
                    case 0:
                        type = parser.getText();
                        break;
                    case 1:
                        if ("event".equals(type)) {
                            eventType = parser.getText();
                        }
                        break;
                    case 2:
                        String name = parser.getText();
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        if (token == JsonToken.START_OBJECT) {
                            token = parser.nextToken();//move first tag key
                        }
                        while (token != null && token != JsonToken.END_OBJECT) {
                            String key = parser.getCurrentName();
                            token = parser.nextToken();//move to value
                            token = parser.nextToken();//move to next key
                        }
                        break;
                    case 8:
                        break;
                    case 9://only for transaction children
                        token = parser.nextToken();
                        while (token != null && token != JsonToken.END_ARRAY) {
                            if ("transaction".equals(type)) {
                                parseMessage(parser);
                            }
                            token = parser.nextToken();//move to next message
                        }
                        break;
                }
            }
            token = parser.nextToken();//move to next value
            index++;
        }
    }
}
