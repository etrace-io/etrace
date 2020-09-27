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

package io.etrace.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class JSONUtil {
    private static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false);

    /**
     * 美化 pretty printer
     *
     * @param obj obj
     * @return {@link String}
     * @throws JsonProcessingException Json处理异常
     */
    public static String beautify(Object obj) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    public static String toString(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static byte[] toBytes(Object object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    public static <T> T toObject(byte[] data, Class<T> clazz) throws IOException {
        return mapper.readValue(data, clazz);
    }

    public static <T> T toObject(String data, Class<T> clazz) throws IOException {
        return mapper.readValue(data, clazz);
    }

    public static <T> List<T> toArray(String data, Class<T> clazz) throws IOException {
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(data, type);
    }

    public static <T> List<T> toArray(byte[] data, Class<T> clazz) throws IOException {
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(data, type);
    }

    public static <T> byte[] toJsonAsBytes(T o) throws IOException {
        return mapper.writeValueAsBytes(o);
    }

    public static <T> String toJson(T o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    public static <T> List<T> toObjectList(String json, Class<T> clazz) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        return mapper.readValue(json, javaType);
    }

    public static <T> List<T> toObjectList(byte[] json, Class<T> clazz) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        return mapper.readValue(json, javaType);
    }

    public static <T> Set<T> toObjectSet(String json, Class<T> clazz) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(Set.class, clazz);
        return mapper.readValue(json, javaType);
    }

}
