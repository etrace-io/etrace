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

package io.etrace.api.util;

import io.etrace.common.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = false)
public class JpaConverterJson implements AttributeConverter<Object, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaConverterJson.class);

    @Override
    public String convertToDatabaseColumn(Object meta) {
        try {
            return JSONUtil.toString(meta);
        } catch (IOException e) {
            LOGGER.error("==convertToDatabaseColumn==", e);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            try {
                return JSONUtil.toObject(dbData, Object.class);
            } catch (IOException e) {
                LOGGER.error("==convertToEntityAttribute==", e);
                return null;
            }
        }
        return null;
    }
}

