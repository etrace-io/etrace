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

package io.etrace.common.message.trace;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.util.MessageHelper;

import java.io.IOException;
import java.util.Map;

import static io.etrace.common.message.trace.AbstractMessage.*;

public class AbstractMessageHelper {
    public static void setCompleted(Message message, boolean completed) {
        ((AbstractMessage)message).setCompleted(completed);
    }

    public static void addTagsForJsonDecode(Message message, Map<String, String> tags) {
        ((AbstractMessage)message).addTagsForJsonDecode(tags);
    }

    public static void encodeMessageAsArray(Message message, JsonGenerator jg) throws IOException {
        jg.writeStartArray();
        if (message instanceof Event) {
            jg.writeString("event");
        } else if (message instanceof Transaction) {
            jg.writeString("transaction");
        } else if (message instanceof Heartbeat) {
            jg.writeString("heartbeat");
        }
        jg.writeString(MessageHelper.truncate(message.getType(), TYPE_TRUNCATE_SIZE));
        jg.writeString(MessageHelper.truncate(message.getName(), NAME_TRUNCATE_SIZE));
        jg.writeString(MessageHelper.truncate(message.getStatus(), STATUS_TRUNCATE_SIZE));
        jg.writeNumber(message.getId());
        jg.writeNumber(message.getTimestamp());
        jg.writeBoolean(message.isCompleted());
        if (message.getTags() != null && message.getTags().size() > 0) {
            jg.writeStartObject();
            for (Map.Entry<String, String> entry : message.getTags().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                key = MessageHelper.truncate(key, TAG_KEY_TRUNCATE_SIZE);
                value = MessageHelper.truncate(value, TAG_VALUE_TRUNCATE_SIZE);
                jg.writeStringField(key, value);
            }
            jg.writeEndObject();
        } else {
            jg.writeNull();
        }
        if (message instanceof Event) {
            jg.writeString(((Event)message).getData());
        } else if (message instanceof Transaction) {
            Transaction transaction = (Transaction)message;
            jg.writeNumber(transaction.getDuration());
            if (transaction.getChildren() != null && transaction.getChildren().size() > 0) {
                jg.writeStartArray();
                for (Message child : transaction.getChildren()) {
                    encodeMessageAsArray(child, jg);
                }
                jg.writeEndArray();
            } else {
                jg.writeNull();
            }
        } else if (message instanceof Heartbeat) {
            jg.writeString(((Heartbeat)message).getData());
        }

        jg.writeEndArray();
    }
}
