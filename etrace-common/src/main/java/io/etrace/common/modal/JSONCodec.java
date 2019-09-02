package io.etrace.common.modal;

import com.fasterxml.jackson.core.*;
import com.google.common.base.Charsets;
import io.etrace.common.Constants;
import io.etrace.common.modal.impl.EventImpl;
import io.etrace.common.modal.impl.HeartbeatImpl;
import io.etrace.common.modal.impl.TransactionImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

// todo: 重新调整该类，移除过期的
public class JSONCodec {
    private static JsonFactory jsonFactory = new JsonFactory();

    public static void encodeAsJson(EsightData esightData, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("tableName", esightData.getTableName());
        generator.writeStringField("type", esightData.getType());
        generator.writeNumberField("chkPresentDuration", esightData.getChkPresentDuration());
        if (esightData.getData() != null) {
            generator.writeFieldName("data");
            generator.writeStartObject();
            for (Map.Entry<String, Object> entry : esightData.getData().entrySet()) {
                generator.writeObjectField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();
        } else {
            generator.writeNullField("data");
        }
        if (esightData.getUpdateData() != null) {
            generator.writeFieldName("updateData");
            generator.writeStartObject();

            generator.writeFieldName("data");
            generator.writeStartObject();
            for (Map.Entry<String, Object> entry : esightData.getUpdateData().getData().entrySet()) {
                generator.writeObjectField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();

            generator.writeFieldName("condition");
            generator.writeStartObject();
            for (Map.Entry<String, Object> entry : esightData.getUpdateData().getCondition().entrySet()) {
                generator.writeObjectField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();

            generator.writeEndObject();
        } else {
            generator.writeNullField("updateData");
        }

        generator.writeEndObject();
        generator.flush();
    }

    //    public static EsightData decodeEsight(byte[] data) throws IOException {
    //        JsonParser parser = null;
    //        try {
    //            ByteArrayInputStream input = new ByteArrayInputStream(data);
    //            parser = jsonFactory.createJsonParser(input);
    //            JsonToken token = parser.nextToken();
    //            if (token == JsonToken.VALUE_NULL) {
    //                return null;
    //            }
    //            if (token != JsonToken.START_OBJECT) {
    //                throw new IllegalArgumentException("Bad json data");
    //            }
    //            token = parser.nextToken();//move to first field
    //            if (token == JsonToken.END_OBJECT) {
    //                return null;
    //            }
    //
    //            EsightData esightData = new EsightData();
    //
    //            CallStack callStack = new CallStack();
    //            while (token != JsonToken.END_OBJECT && token != null) {
    //                String fieldName = parser.getCurrentName();
    //                token = parser.nextToken();//move to value
    //                if ("appId".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setAppId(parser.getText());
    //                    }
    //                } else if ("hostIp".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setHostIp(parser.getText());
    //                    }
    //                } else if ("hostName".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setHostName(parser.getText());
    //                    }
    //                } else if ("requestId".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setRequestId(parser.getText());
    //                    }
    //                } else if ("id".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setId(parser.getText());
    //                    }
    //                } else if ("message".equals(fieldName)) {
    //                    if (token == JsonToken.VALUE_NULL) {
    //                        token = parser.nextToken();
    //                        continue;
    //                    }
    //                    if (token != JsonToken.START_OBJECT) {
    //                        throw new IllegalArgumentException("Bad json data with bad message data");
    //                    }
    //                    callStack.setMessage(decodeMessage(parser));
    //                } else if ("cluster".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setCluster(parser.getText());
    //                    }
    //                } else if ("ezone".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setEzone(parser.getText());
    //                    }
    //                } else if ("idc".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setIdc(parser.getText());
    //                    }
    //                } else if ("instance".equals(fieldName)) {
    //                    if (token != JsonToken.VALUE_NULL) {
    //                        callStack.setInstance(parser.getText());
    //                    }
    //                }
    //                token = parser.nextToken();//move to next filed
    //            }
    //            return callStack;
    //        } finally {
    //            if (parser != null) {
    //                parser.close();
    //            }
    //        }
    //
    //    }

    public static void encodeAsJson(CallStack callStack, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("appId", callStack.getAppId());
        generator.writeStringField("hostIp", callStack.getHostIp());
        generator.writeStringField("hostName", callStack.getHostName());
        generator.writeStringField("requestId", callStack.getRequestId());
        generator.writeStringField("id", callStack.getId());
        generator.writeFieldName("message");

        encodeMessage(generator, callStack.getMessage());

        generator.writeStringField("cluster", callStack.getCluster());
        generator.writeStringField("ezone", callStack.getEzone());
        generator.writeStringField("idc", callStack.getIdc());

        generator.writeStringField("mesosTaskId", callStack.getMesosTaskId());
        generator.writeStringField("eleapposLabel", callStack.getEleapposLabel());
        generator.writeStringField("eleapposSlaveFqdn", callStack.getEleapposSlaveFqdn());
        generator.writeStringField("instance", callStack.getInstance());

        generator.writeEndObject();

        generator.flush();
    }

    public static void encodeAsArray(CallStack callStack, JsonGenerator generator) throws IOException {
        generator.writeStartArray();

        generator.writeString(callStack.getAppId());
        generator.writeString(callStack.getHostIp());
        generator.writeString(callStack.getHostName());
        generator.writeString(callStack.getRequestId());
        generator.writeString(callStack.getId());

        callStack.getMessage().encodeMessageAsArray(generator);

        generator.writeString(callStack.getCluster());
        generator.writeString(callStack.getEzone());
        generator.writeString(callStack.getIdc());

        generator.writeString(callStack.getMesosTaskId());
        generator.writeString(callStack.getEleapposLabel());
        generator.writeString(callStack.getEleapposSlaveFqdn());
        generator.writeString(callStack.getInstance());

        generator.writeEndArray();

        generator.flush();
    }

    public static byte[] encodeAsArray(List<CallStack> callStacks) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            generator.writeStartArray();
            for (CallStack callStack : callStacks) {
                encodeAsArray(callStack, generator);
            }
            generator.writeEndArray();
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toByteArray();
    }

    public static byte[] encodeAsJson(List<CallStack> callStacks) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            generator.writeStartArray();
            for (CallStack callStack : callStacks) {
                encodeAsJson(callStack, generator);
            }
            generator.writeEndArray();
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toByteArray();
    }

    public static byte[] encodeAsJson(CallStack callStack) throws IOException {
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

    public static byte[] encodeAsArray(CallStack callStack) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            encodeAsArray(callStack, generator);
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toByteArray();
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

    @Deprecated
    public static List<MessageItem> decodeMessageItems(byte[] data, MessageHeader header) throws IOException {
        List<MessageItem> callStacks;
        JsonParser parser = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            ByteArrayInputStream rawInput = new ByteArrayInputStream(data);
            parser = jsonFactory.createJsonParser(input);
            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NULL) {
                return null;
            }
            callStacks = new ArrayList<>();
            if (token != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Bad json array data");
            }
            JsonLocation location = parser.getCurrentLocation();
            int offset = location.getColumnNr();
            rawInput.skip(offset - 1);
            token = parser.nextToken();//move to first object
            MessageItem messageItem = null;
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token == JsonToken.START_ARRAY) {
                    CallStack callStack = new CallStack();
                    messageItem = new MessageItem(callStack, header);
                    token = parser.nextToken();//move to first value
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
                                        throw new IllegalArgumentException("Bad json data");
                                    }
                                    callStack.setMessage(decodeMessageJson(parser));
                                case 6:
                                    callStack.setCluster(parser.getText());
                                    break;
                                case 7:
                                    callStack.setEzone(parser.getText());
                                    break;
                                case 8:
                                    callStack.setIdc(parser.getText());
                                    break;
                            }
                        }
                        token = parser.nextToken();//move to next value
                        index++;
                    }
                    messageItem.setRequestId(callStack.getRequestId());
                    callStacks.add(messageItem);
                }
                location = parser.getCurrentLocation();
                byte[] d = new byte[location.getColumnNr() - offset];
                rawInput.read(d);
                rawInput.skip(1);
                if (messageItem != null) {
                    messageItem.setMessageData(d);
                }
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

    @Deprecated
    public static List<CallStack> decodeListJson(byte[] data) throws IOException {
        List<CallStack> callStacks;
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
            token = parser.nextToken();//move to first object
            while (token != JsonToken.END_ARRAY && token != null) {
                if (token == JsonToken.START_ARRAY) {
                    CallStack callStack = new CallStack();
                    token = parser.nextToken();//move to first value
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
                                        throw new IllegalArgumentException("Bad json data");
                                    }
                                    callStack.setMessage(decodeMessageJson(parser));
                                case 6:
                                    callStack.setCluster(parser.getText());
                                    break;
                                case 7:
                                    callStack.setEzone(parser.getText());
                                    break;
                                case 8:
                                    callStack.setIdc(parser.getText());
                                    break;
                            }
                        }
                        token = parser.nextToken();//move to next value
                        index++;
                    }
                    callStacks.add(callStack);
                }
                token = parser.nextToken();//move to next call stack
            }
            return callStacks;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    @Deprecated
    public static List<CallStack> decodeList(byte[] data) throws IOException {
        List<CallStack> callStacks;
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
                token = parser.nextToken();//move to object
                if (token != JsonToken.START_OBJECT) {
                    continue;
                }
                CallStack callStack = new CallStack();
                token = parser.nextToken();//move to first filed
                while (token != JsonToken.END_OBJECT && token != null) {
                    String fieldName = parser.getCurrentName();
                    token = parser.nextToken();//move to value
                    if ("appId".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setAppId(parser.getText());
                        }
                    } else if ("hostIp".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setHostIp(parser.getText());
                        }
                    } else if ("hostName".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setHostName(parser.getText());
                        }
                    } else if ("requestId".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setRequestId(parser.getText());
                        }
                    } else if ("id".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setId(parser.getText());
                        }
                    } else if ("message".equals(fieldName)) {
                        if (token != JsonToken.START_OBJECT) {
                            throw new IllegalArgumentException("Bad json data");
                        }
                        callStack.setMessage(decodeMessage(parser));
                    } else if ("cluster".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setCluster(parser.getText());
                        }
                    } else if ("ezone".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setEzone(parser.getText());
                        }
                    } else if ("idc".equals(fieldName)) {
                        if (token != JsonToken.VALUE_NULL) {
                            callStack.setIdc(parser.getText());
                        }
                    }
                    token = parser.nextToken();//move to next filed
                }
                callStacks.add(callStack);
            }
            parser.close();
            return callStacks;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static CallStack decodeJson(String data) throws IOException {
        JsonParser parser = null;
        try {
            JsonFactory f = new JsonFactory();
            parser = f.createJsonParser(data);
            return decodeCallStackJson(parser);
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static CallStack decode(String data) throws IOException {
        JsonParser parser = null;
        try {
            parser = jsonFactory.createJsonParser(data);
            CallStack callStack = decodeCallStack(parser);
            parser.close();
            return callStack;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static CallStack decodeJson(byte[] data) throws IOException {
        JsonParser parser = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            parser = jsonFactory.createJsonParser(input);
            return decodeCallStackJson(parser);
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static CallStack decode(byte[] data) throws IOException {
        JsonParser parser = null;
        try {
            JsonFactory f = new JsonFactory();
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            parser = f.createJsonParser(input);
            CallStack callStack = decodeCallStack(parser);
            parser.close();
            return callStack;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static CallStack decodeCallStackJson(JsonParser parser) throws IOException {
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
        CallStack callStack = new CallStack();
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
                        callStack.setMessage(decodeMessageJson(parser));
                        break;
                    case 6:
                        callStack.setCluster(parser.getText());
                        break;
                    case 7:
                        callStack.setEzone(parser.getText());
                        break;
                    case 8:
                        callStack.setIdc(parser.getText());
                        break;
                    case 9:
                        callStack.setMesosTaskId(parser.getText());
                        break;
                    case 10:
                        callStack.setEleapposLabel(parser.getText());
                        break;
                    case 11:
                        callStack.setEleapposSlaveFqdn(parser.getText());
                        break;
                    case 12:
                        callStack.setInstance(parser.getText());
                        break;

                }
            }
            token = parser.nextToken();//move to next value
            index++;
        }
        return callStack;
    }

    private static CallStack decodeCallStack(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token != JsonToken.START_OBJECT) {
            throw new IllegalArgumentException("Bad json data");
        }
        token = parser.nextToken();//move to first field
        if (token == JsonToken.END_OBJECT) {
            return null;
        }
        CallStack callStack = new CallStack();
        while (token != JsonToken.END_OBJECT && token != null) {
            String fieldName = parser.getCurrentName();
            token = parser.nextToken();//move to value
            if ("appId".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setAppId(parser.getText());
                }
            } else if ("hostIp".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setHostIp(parser.getText());
                }
            } else if ("hostName".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setHostName(parser.getText());
                }
            } else if ("requestId".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setRequestId(parser.getText());
                }
            } else if ("id".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setId(parser.getText());
                }
            } else if ("message".equals(fieldName)) {
                if (token == JsonToken.VALUE_NULL) {
                    token = parser.nextToken();
                    continue;
                }
                if (token != JsonToken.START_OBJECT) {
                    throw new IllegalArgumentException("Bad json data with bad message data");
                }
                callStack.setMessage(decodeMessage(parser));
            } else if ("cluster".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setCluster(parser.getText());
                }
            } else if ("ezone".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setEzone(parser.getText());
                }
            } else if ("idc".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setIdc(parser.getText());
                }
            } else if ("instance".equals(fieldName)) {
                if (token != JsonToken.VALUE_NULL) {
                    callStack.setInstance(parser.getText());
                }
            }
            token = parser.nextToken();//move to next filed
        }
        return callStack;
    }

    private static Message decodeMessage(JsonParser parser) throws IOException {
        Message message = null;
        JsonToken token = parser.nextToken();//move first filed
        while (token != null && token != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            token = parser.nextToken(); // move to value, or START_OBJECT/START_ARRAY
            if ("_type".equals(fieldName)) {
                String type = parser.getText();
                if ("event".equals(type)) {
                    message = new EventImpl();
                } else if ("transaction".equals(type)) {
                    message = new TransactionImpl();
                } else if ("heartbeat".equals(type)) {
                    message = new HeartbeatImpl();
                }
            } else if ("type".equals(fieldName)) {
                if (message != null) {
                    message.setType(parser.getText());
                }
            } else if ("name".equals(fieldName)) {
                if (message != null) {
                    message.setName(parser.getText());
                }
            } else if ("status".equals(fieldName)) {
                if (message != null && token != JsonToken.VALUE_NULL) {
                    message.setStatus(parser.getText());
                }
            } else if ("id".equals(fieldName)) {
                if (message != null && token != JsonToken.VALUE_NULL) {
                    message.setId(parser.getLongValue());
                }
            } else if ("timestamp".equals(fieldName)) {
                if (message != null && token != JsonToken.VALUE_NULL) {
                    ((AbstractMessage)message).setTimestamp(parser.getLongValue());
                }
            } else if ("complete".equals(fieldName)) {
                if (message != null && token != JsonToken.VALUE_NULL) {
                    ((AbstractMessage)message).setCompleted(parser.getBooleanValue());
                }
            } else if ("tags".equals(fieldName)) {
                if (token == JsonToken.VALUE_NULL) {
                    token = parser.nextToken();
                    continue;
                }
                if (token == JsonToken.START_OBJECT) {
                    token = parser.nextToken();//move first tag key
                }
                Map<String, String> tags = newHashMap();
                while (token != null && token != JsonToken.END_OBJECT) {
                    String key = parser.getCurrentName();
                    token = parser.nextToken();//move to value
                    if (message != null) {
                        tags.put(key, parser.getText());
                    }
                    token = parser.nextToken();//move to next key
                }
                if (message instanceof AbstractMessage) {
                    ((AbstractMessage)message).addTagsForJsonDecode(tags);
                }
            } else if ("data".equals(fieldName)) {
                if (message instanceof Event) {
                    if (token != JsonToken.VALUE_NULL) {
                        ((Event)message).setData(parser.getText());
                    }
                } else if (message instanceof Heartbeat) {
                    if (token != JsonToken.VALUE_NULL) {
                        ((Heartbeat)message).setData(parser.getText());
                    }
                }
            } else if ("duration".equals(fieldName)) {
                if (message instanceof Transaction) {
                    ((Transaction)message).setDuration(parser.getLongValue());
                }
            } else if ("children".equals(fieldName)) {
                if (token == JsonToken.VALUE_NULL) {
                    token = parser.nextToken();
                    continue;
                }
                token = parser.nextToken();
                if (token == JsonToken.END_ARRAY) {
                    token = parser.nextToken();
                    continue;
                }
                if (token != JsonToken.START_OBJECT) {
                    throw new IllegalArgumentException("Bad json data with transaction children");
                }
                while (token != null && token != JsonToken.END_ARRAY) {
                    if (message instanceof Transaction) {
                        ((Transaction)message).addChild(decodeMessage(parser));
                    }
                    token = parser.nextToken();//move to next message
                }
            }
            token = parser.nextToken();
        }
        return message;
    }

    private static Message decodeMessageJson(JsonParser parser) throws IOException {
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
                            ((AbstractMessage)message).setCompleted(parser.getBooleanValue());
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
                            ((AbstractMessage)message).addTagsForJsonDecode(tags);
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
                                ((Transaction)message).addChild(decodeMessageJson(parser));
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

    public static void writeRedisStats(JsonGenerator jg, Transaction transaction) throws IOException {
        List<Message> childs = transaction.getChildren();
        if (childs == null || childs.size() == 0) {
            jg.writeNull();
            return;
        }
        jg.writeStartObject();
        long redisCount = 0;
        for (Message child : transaction.getChildren()) {
            if (!(child instanceof RedisStats)) {
                continue;
            }
            RedisStats redisStats = (RedisStats)child;
            redisCount += redisStats.getAllCount();
            jg.writeStringField(redisStats.getUrl(), encodeAsJson(redisStats));
        }
        jg.writeStringField(Constants.REDIS_TYPE, String.valueOf(redisCount));
        jg.writeEndObject();
    }

    private static String encodeAsJson(RedisStats redisStats) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = null;
        try {
            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            generator.writeStartObject();
            generator.writeStringField("url", redisStats.getUrl());
            generator.writeNumberField("timestamp", redisStats.getTimestamp());
            generator.writeFieldName("commands");
            generator.writeStartArray();
            Map<String, RedisStats.RedisCommandStats> commands = redisStats.getCommands();
            if (commands != null && commands.size() > 0) {
                for (RedisStats.RedisCommandStats redisCommandStats : commands.values()) {
                    generator.writeStartObject();
                    generator.writeStringField("command", redisCommandStats.getCommand());
                    generator.writeNumberField("succeedCount", redisCommandStats.getSucceedCount());
                    generator.writeNumberField("failCount", redisCommandStats.getFailCount());
                    generator.writeNumberField("durationSucceedSum", redisCommandStats.getDurationSucceedSum());
                    generator.writeNumberField("durationFailSum", redisCommandStats.getDurationFailSum());
                    generator.writeNumberField("maxDuration", redisCommandStats.getMaxDuration());
                    generator.writeNumberField("minDuration", redisCommandStats.getMinDuration());
                    generator.writeNumberField("responseCount", redisCommandStats.getResponseCount());
                    generator.writeNumberField("hitCount", redisCommandStats.getHitCount());
                    generator.writeNumberField("responseSizeSum", redisCommandStats.getResponseSizeSum());
                    generator.writeNumberField("maxResponseSize", redisCommandStats.getMaxResponseSize());
                    generator.writeNumberField("minResponseSize", redisCommandStats.getMinResponseSize());
                    generator.writeEndObject();
                }
            }
            generator.writeEndArray();
            generator.writeEndObject();
            generator.flush();
        } finally {
            if (generator != null) {
                generator.close();
            }
        }
        return baos.toString(Charsets.UTF_8.name());
    }

    public static RedisStats decodeRedisStatsJson(String data) throws IOException {
        JsonParser parser = null;
        try {
            parser = jsonFactory.createJsonParser(data);
            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NULL) {
                return null;
            }
            if (token != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("Bad json data");
            }
            token = parser.nextToken();//move to first field
            RedisStats redisStats = new RedisStats(null);
            while (token != null && token != JsonToken.END_OBJECT) {
                String key = parser.getCurrentName();
                token = parser.nextToken();//move to value
                if ("url".equals(key)) {
                    redisStats.setUrl(parser.getText());
                } else if ("timestamp".equals(key)) {
                    redisStats.setTimestamp(parser.getLongValue());
                } else if ("commands".equals(key)) {
                    if (token != JsonToken.START_ARRAY) {
                        throw new IllegalArgumentException("Bad json data");
                    }
                    token = parser.nextToken();
                    while (token != null && token != JsonToken.END_ARRAY) {
                        if (token != JsonToken.START_OBJECT) {
                            throw new IllegalArgumentException("Bad json data");
                        }
                        decodeRedisCommandStatsJson(redisStats, parser);
                        token = parser.nextToken();
                    }
                }
                token = parser.nextToken();//move to next key
            }
            return redisStats;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static void decodeRedisCommandStatsJson(RedisStats redisStats, JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        RedisStats.RedisCommandStats redisCommandStats = redisStats.newRedisCommandStats();
        while (token != null && token != JsonToken.END_OBJECT) {
            String key = parser.getCurrentName();
            token = parser.nextToken();//move to value
            switch (key) {
                case "command":
                    redisCommandStats.setCommand(parser.getText());
                    break;
                case "succeedCount":
                    redisCommandStats.setSucceedCount(parser.getLongValue());
                    break;
                case "failCount":
                    redisCommandStats.setFailCount(parser.getLongValue());
                    break;
                case "durationSucceedSum":
                    redisCommandStats.setDurationSucceedSum(parser.getLongValue());
                    break;
                case "durationFailSum":
                    redisCommandStats.setDurationFailSum(parser.getLongValue());
                    break;
                case "maxDuration":
                    redisCommandStats.setMaxDuration(parser.getLongValue());
                    break;
                case "minDuration":
                    redisCommandStats.setMinDuration(parser.getLongValue());
                    break;
                case "responseCount":
                    redisCommandStats.setResponseCount(parser.getLongValue());
                    break;
                case "hitCount":
                    redisCommandStats.setHitCount(parser.getLongValue());
                    break;
                case "responseSizeSum":
                    redisCommandStats.setResponseSizeSum(parser.getLongValue());
                    break;
                case "maxResponseSize":
                    redisCommandStats.setMaxResponseSize(parser.getLongValue());
                    break;
                case "minResponseSize":
                    redisCommandStats.setMinResponseSize(parser.getLongValue());
                    break;
                default:
                    break;
            }
            token = parser.nextToken();
        }
        redisStats.add(redisCommandStats);
    }
}
