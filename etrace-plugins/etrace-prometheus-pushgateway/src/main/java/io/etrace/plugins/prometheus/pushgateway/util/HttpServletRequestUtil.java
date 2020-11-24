package io.etrace.plugins.prometheus.pushgateway.util;

import javax.servlet.http.HttpServletRequest;

public class HttpServletRequestUtil {

    public static String getRemoteIp(HttpServletRequest request) {
        String ipForwarded = request.getHeader("x-forwarded-for");
        if (ipForwarded == null) {
            return request.getRemoteAddr();
        } else {
            return ipForwarded;
        }
    }
}
