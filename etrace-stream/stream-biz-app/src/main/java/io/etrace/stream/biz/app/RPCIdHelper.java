package io.etrace.stream.biz.app;

import com.google.common.base.Strings;

public class RPCIdHelper {
    public static int getLevel(String rpcId) {
        if (Strings.isNullOrEmpty(rpcId)) {
            return -1;
        } else {
            String[] str = rpcId.split("\\.");
            return str.length;
        }
    }
}
