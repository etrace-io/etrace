package io.etrace.plugins.prometheus.pushgateway.convert;

import io.etrace.common.message.metric.impl.AbstractMetric;
import io.etrace.common.message.metric.impl.CounterImpl;
import io.etrace.common.message.metric.impl.GaugeImpl;
import io.etrace.plugins.prometheus.pushgateway.model.EtraceExtendInfo;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricSampleV1;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricV1;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class EtraceHelper {
    public static List<AbstractMetric> convertPrometheusMetricToEtraceMetric(
        List<PrometheusMetricV1> prometheusMetricV1List) {
        if (CollectionUtils.isEmpty(prometheusMetricV1List)) {
            return Collections.emptyList();
        }
        List<AbstractMetric> traceMetricList = new ArrayList<>(prometheusMetricV1List.size());
        for (PrometheusMetricV1 prometheusMetricV1 : prometheusMetricV1List) {
            for (PrometheusMetricSampleV1 sample : prometheusMetricV1.samples) {
                AbstractMetric abstractMetric = null;
                Map<String, String> traceTags = buildTraceTags(sample.labelNames, sample.labelValues);
                switch (prometheusMetricV1.type) {
                    case GAUGE:
                        abstractMetric = new GaugeImpl(null, sample.name);
                        addMetricTags(abstractMetric, traceTags);
                        ((GaugeImpl)abstractMetric).value(sample.value);
                    case COUNTER:
                        abstractMetric = new CounterImpl(null, sample.name);
                        addMetricTags(abstractMetric, traceTags);
                        // 可能会丢失精度
                        ((CounterImpl)abstractMetric).value((long)sample.value);
                    case SUMMARY:
                        abstractMetric = new CounterImpl(null, sample.name);
                        addMetricTags(abstractMetric, traceTags);
                        // 可能会丢失精度
                        ((CounterImpl)abstractMetric).value((long)sample.value);
                    default:
                        break;

                }
                if (null != abstractMetric) {
                    traceMetricList.add(abstractMetric);
                }
            }
        }
        return traceMetricList;
    }

    private static void addMetricTags(AbstractMetric abstractMetric, Map<String, String> tagMap) {
        if (null == abstractMetric) {
            return;
        }
        if (null != tagMap) {
            for (Map.Entry<String, String> entry : tagMap.entrySet()) {
                abstractMetric.addTag(entry.getKey(), entry.getValue());
            }
        }
    }

    private static Map<String, String> buildTraceTags(List<String> labelNames, List<String> labelValues) {
        if (null == labelNames || null == labelValues) {
            return new HashMap<>();
        }
        Map<String, String> tagMap = new HashMap<>(labelNames.size());
        for (int i = 0; i < labelNames.size(); i++) {
            tagMap.put(labelNames.get(i), labelValues.get(i));
        }
        return tagMap;
    }

    public static EtraceExtendInfo buildEtraceExtendInfo(String remoteHost, String remoteIp) {
        EtraceExtendInfo etraceExtendInfo = new EtraceExtendInfo();
        etraceExtendInfo.setRemoteHostIp(remoteIp);
        etraceExtendInfo.setRemoteHostName(remoteHost);
        return etraceExtendInfo;
    }
}
