package io.etrace.common.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.List;

public class RequestIdHelper {
    public static String getRootAppId(String requestId) {
        if (Strings.isNullOrEmpty(requestId)) {
            return null;
        }
        List<String> list = Splitter.on("^^").splitToList(requestId);
        if (list.size() >= 2) {
            return list.get(0);
        }
        return null;
    }

    public static String removeRootAppId(String requestId) {
        if (Strings.isNullOrEmpty(requestId)) {
            return null;
        }
        int index = requestId.indexOf("^^");
        if (index > 0 && index + 2 <= requestId.length()) {
            return requestId.substring(index + 2);
        }
        return requestId;
    }

    public static long getTimestamp(String requestId) {
        int index = requestId.lastIndexOf("|");
        try {
            return Long.valueOf(requestId.substring(index + 1));
        } catch (Exception ignore) {
            return 0;
        }
    }

    public static String getReqId(String requestId) {
        int tsIndex = requestId.indexOf("|");
        if (tsIndex < 0) {
            return requestId;
        } else {
            return requestId.substring(0, tsIndex);
        }
    }

}
