package io.etrace.plugins.prometheus.pushgateway.convert;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.TextFormat;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * merge from io.prometheus.client.exporter.PushGateway
 */
public class PrometheusPushgatewayWriter {

    public static String buildPushGatewayUrl(String job, Map<String, String> groupingKey)
        throws UnsupportedEncodingException {
        String url = "/metrics/";
        if (job.contains("/")) {
            url += "job@base64/" + base64url(job);
        } else {
            url += "job/" + URLEncoder.encode(job, "UTF-8");
        }
        if (groupingKey != null) {
            for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
                if (entry.getValue().contains("/")) {
                    url += "/" + entry.getKey() + "@base64/" + base64url(entry.getValue());
                } else {
                    url += "/" + entry.getKey() + "/" + URLEncoder.encode(entry.getValue(), "UTF-8");
                }
            }
        }
        return url;
    }

    public static String buildMetricJson(List<Collector.MetricFamilySamples> mfs) throws IOException {
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, Collections.enumeration(mfs));
        return writer.toString();
    }

    public static String base64url(String v) {
        // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
        try {
            return DatatypeConverter.printBase64Binary(v.getBytes("UTF-8")).replace("+", "-").replace("/", "_");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);  // Unreachable.
        }
    }
}
