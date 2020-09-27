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

package io.etrace.common.message.metric.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransferUtils {

    public static Map<String, String> parseTags(JsonParser parser) throws IOException {

        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        ensureStartObjectToken(token);
        token = parser.nextToken();
        Map<String, String> tags;
        tags = new HashMap<>();
        while (token != null && token != JsonToken.END_OBJECT) {
            String key = parser.getText();
            parser.nextToken();
            String value = parser.getText();
            tags.put(key, value);
            token = parser.nextToken();
        }
        return tags;
    }

    public static Map<String, Double> parseSimpleFields(JsonParser parser) throws IOException {

        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        ensureStartArrayToken(token);

        Map<String, Double> fields = new HashMap<>();
        token = parser.nextToken();
        if (token == JsonToken.END_ARRAY) {
            return new HashMap<>();
        }
        ensureStartArrayToken(token);
        while (token != null && token != JsonToken.END_ARRAY) {
            int index = 0;
            String fieldName = null;
            token = parser.nextToken();
            while (token != JsonToken.END_ARRAY) {
                switch (index) {
                    case 0:
                        fieldName = parser.getText();
                        break;
                    case 2:
                        fields.put(fieldName, parser.getDoubleValue());
                        break;
                }
                token = parser.nextToken();
                index++;
            }
            token = parser.nextToken();
        }
        return fields;
    }

    public static Map<String, Field> parseFields(JsonParser parser) throws IOException {
        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        ensureStartArrayToken(token);

        Map<String, Field> fields = new HashMap<>();
        token = parser.nextToken();
        if (token == JsonToken.END_ARRAY) {
            return new HashMap<>();
        }

        ensureStartArrayToken(token);
        while (token != null && token != JsonToken.END_ARRAY) {
            int index = 0;
            String fieldName = null;
            Field field = new Field();
            token = parser.nextToken();
            while (token != JsonToken.END_ARRAY) {
                switch (index) {
                    case 0:
                        fieldName = parser.getText();
                        break;
                    case 1:
                        field.setAggregateType(AggregateType.valueOf(parser.getText()));
                        break;
                    case 2:
                        field.setValue(parser.getDoubleValue());
                        break;
                }
                token = parser.nextToken();
                index++;
            }
            token = parser.nextToken();
            fields.put(fieldName, field);
        }
        return fields;
    }

    public static void ensureStartObjectToken(JsonToken token) {
        if (token != JsonToken.START_OBJECT) {
            throw new IllegalArgumentException("Bad json data with bad metric data");
        }
    }

    public static void ensureStartArrayToken(JsonToken token) {
        if (token != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("Bad json data with bad metric data");
        }
    }

}
