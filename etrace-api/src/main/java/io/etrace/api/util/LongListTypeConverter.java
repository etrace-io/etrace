package io.etrace.api.util;

import io.etrace.common.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.List;

@Converter(autoApply = false)
public class LongListTypeConverter implements AttributeConverter<List<Long>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaConverterJson.class);

    @Override
    public String convertToDatabaseColumn(List<Long> longs) {
        try {
            return JSONUtil.toJson(longs);
        } catch (Exception e) {
            LOGGER.error("==convertToDatabaseColumn==", e);
            return null;
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String s) {
        try {
            if (StringUtils.isEmpty(s)) {
                return null;
            }
            return JSONUtil.toObjectList(s, Long.class);

        } catch (IOException e) {
            LOGGER.error("==convertToDatabaseColumn==", e);
        }
        return null;
    }
}
