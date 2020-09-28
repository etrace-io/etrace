package io.etrace.stream.biz.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLPatternParser {
    private static Pattern urlPattern = Pattern.compile("^\\d+$");

    public static String parseURL(String url) {
        if (url == null) {
            return null;
        }
        int index = url.indexOf("?");
        String raw = url;
        if (index >= 0) {
            raw = url.substring(0, index);
        }
        StringBuilder sb = new StringBuilder();
        String[] strs = raw.split("/");
        for (String ss : strs) {
            Matcher matcher = urlPattern.matcher(ss);
            if (!matcher.find()) {
                if (ss.length() > 30) {
                    sb.append("{+w}");
                } else {
                    sb.append(ss);
                }
            } else {
                sb.append("{+d}");
            }
            sb.append("/");
        }
        return sb.toString();
    }
}
