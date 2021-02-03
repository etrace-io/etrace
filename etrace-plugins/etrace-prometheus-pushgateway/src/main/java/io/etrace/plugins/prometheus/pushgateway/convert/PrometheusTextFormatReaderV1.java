package io.etrace.plugins.prometheus.pushgateway.convert;

import com.google.common.collect.Maps;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricSampleV1;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricV1;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusTypeEnumV1;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.etrace.plugins.prometheus.pushgateway.constants.PushGatewayConstants.*;

/**
 * parse the text with version 0.0.4 to metrics
 */
public class PrometheusTextFormatReaderV1 {

    public static Map<String, String> parseLables(String requestURI) {
        Map<String, String> groupingMap = new HashMap<>();
        String[] splits = requestURI.split("/");
        if (splits.length <= 4 || splits.length % 2 != 0) {
            return groupingMap;
        }
        for (int i = 4; i < splits.length - 1; i = i + 2) {
            String key = splits[i];
            String value = splits[i + 1];
            if (key.endsWith(BASE64_POSTFIX)) {
                key = key.substring(0, key.length() - BASE64_POSTFIX.length());
                value = decodeBase64(value);
            }
            groupingMap.put(key, value);
        }
        return groupingMap;
    }

    public static List<PrometheusMetricV1> parse(String text, String jobName, Map<String, String> lables) {
        if (StringUtils.isEmpty(jobName)) {
            jobName = UNKNOWN_JOB;
        }
        if (null == lables) {
            lables = new HashMap<>();
        }
        lables.put(JOB_NAME_KEY, jobName);
        return doParse(text, lables);
    }

    private static List<PrometheusMetricV1> doParse(String text, Map<String, String> globalTags) {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("the parse text must not be null");
        }
        if (null == globalTags) {
            globalTags = Maps.newHashMap();
        }
        long currentTimeMillis = System.currentTimeMillis();

        List<PrometheusMetricV1> mfs = new ArrayList<>();
        String[] lines = text.split("\n");
        if (lines.length == 0) {
            return Collections.emptyList();
        }
        String metricName;
        PrometheusTypeEnumV1 metricType;
        String help = null;
        PrometheusMetricV1 metricFamilySamples;
        List<String> lableNames = new ArrayList<>(globalTags.size());
        List<String> lableValues = new ArrayList<>(globalTags.size());
        for (Map.Entry<String, String> entry : globalTags.entrySet()) {
            if (null != entry.getKey() && null != entry.getValue()) {
                lableNames.add(entry.getKey());
                lableValues.add(entry.getValue());
            }
        }
        List<PrometheusMetricSampleV1> samples = null;
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            if (line.charAt(0) == '#') {
                if (line.startsWith("# HELP")) {
                    help = parseHelp(line);
                    samples = new ArrayList<>();
                } else if (line.startsWith("# TYPE")) {
                    metricName = parseMetricName(line);
                    metricType = paseType(line);
                    metricFamilySamples = new PrometheusMetricV1(metricName, metricType, help, samples);
                    mfs.add(metricFamilySamples);
                }
            } else {
                PrometheusMetricSampleV1 prometheusMetricSampleV1 = parseMetric(line, currentTimeMillis);
                samples.add(prometheusMetricSampleV1);
                if (!CollectionUtils.isEmpty(lableNames)) {
                    prometheusMetricSampleV1.labelNames.addAll(lableNames);
                    prometheusMetricSampleV1.labelValues.addAll(lableValues);
                }
            }
        }
        return mfs;
    }

    private static String parseMetricName(String line) {
        return line.substring(7, line.indexOf(' ', 7));
    }

    private static String parseHelp(String line) {
        return line.substring(line.lastIndexOf(' ') + 1);
    }

    private static PrometheusTypeEnumV1 paseType(String line) {
        String typeString = line.substring(line.lastIndexOf(' ') + 1);
        switch (typeString) {
            case "counter":
                return PrometheusTypeEnumV1.COUNTER;
            case "gauge":
                return PrometheusTypeEnumV1.GAUGE;
            case "summary":
                return PrometheusTypeEnumV1.SUMMARY;
            case "histogram":
                return PrometheusTypeEnumV1.HISTOGRAM;
            default:
                return PrometheusTypeEnumV1.UNTYPED;
        }
    }

    private static PrometheusMetricSampleV1 parseMetric(String line, long ms) {
        int index = line.indexOf("{");
        int endIndex = line.indexOf("}");

        if (index < 0 || endIndex < 0) {
            return parseMetricWithNoTags(line, ms);
        }
        Double value = Double.valueOf(line.substring(endIndex + 2, line.length()));
        String sampleName = line.substring(0, index);
        String[] tagValues = line.substring(index + 1, endIndex - 1).split(",");
        List<String> tagKeyList = new ArrayList<>(tagValues.length);
        List<String> tagValueList = new ArrayList<>(tagValues.length);
        for (String tagValue : tagValues) {
            String[] tagValueArray = tagValue.split("=");
            String tagKey = tagValueArray[0];
            String tagKeyValue = tagValueArray[1].substring(1, tagValueArray[1].length() - 1);
            if (tagKey.endsWith(BASE64_POSTFIX)) {
                tagKeyValue = decodeBase64(tagKeyValue);
            }
            tagKeyList.add(tagKey);
            tagValueList.add(tagKeyValue);
        }
        return new PrometheusMetricSampleV1(sampleName, tagKeyList, tagValueList, value, ms);
    }

    private static PrometheusMetricSampleV1 parseMetricWithNoTags(String line, long ms) {
        int index = line.indexOf(" ");
        Double value = Double.valueOf(line.substring(index + 1, line.length()));
        String sampleName = line.substring(0, index);
        return new PrometheusMetricSampleV1(sampleName, null, null, value, ms);
    }

    public static String decodeBase64(String value) {
        try {
            return new String(DatatypeConverter.parseBase64Binary(value), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String parseJobName(String requestURI) {
        String[] splits = requestURI.split("/");
        if (splits.length <= 4 || splits.length % 2 != 0) {
            throw new IllegalArgumentException("the url maybe not prometheus");
        }
        return null;
    }

}
