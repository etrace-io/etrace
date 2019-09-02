package io.etrace.common.util;

import com.google.common.base.Strings;

/**
 * Created by jie.huang on 15/8/18.
 */
public class MessageHelper {
    public static String truncate(String value, int maxSize) {
        if (Strings.isNullOrEmpty(value)) {
            return value;
        }
        return value.length() <= maxSize ? value : value.substring(0, maxSize);
    }
}
