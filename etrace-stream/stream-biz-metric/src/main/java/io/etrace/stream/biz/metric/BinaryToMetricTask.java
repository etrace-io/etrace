package io.etrace.stream.biz.metric;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.codec.FramedMetricMessageCodecV1;
import io.etrace.common.pipeline.Component;
import io.etrace.stream.biz.metric.event.MetricWithHeader;
import io.etrace.stream.core.codec.AbstractSnappyDecodeTask;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.*;

import static com.google.common.collect.Sets.newHashSet;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BinaryToMetricTask extends AbstractSnappyDecodeTask {
    private static final String SKIP_TS_VALIDATE_SOURCES = "ignoreSources";
    private FramedMetricMessageCodecV1 metricMessageCodec = new FramedMetricMessageCodecV1();
    private Set<String> ignoreSources = newHashSet();

    public BinaryToMetricTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        Optional.ofNullable(params.get(SKIP_TS_VALIDATE_SOURCES)).ifPresent(
            values -> Arrays.stream(values.toString().split(",")).forEach(ignoreSources::add));
    }

    @Override
    public void decode(byte[] data) throws Exception {
        MetricMessage metricMessage = metricMessageCodec.decodeToLegacyVersion(data);

        for (Metric metric : metricMessage.getMetrics()) {
            if (isTsValid(metric.getTimestamp()) || ignoreSources.contains(metric.getSource())) {
                MetricWithHeader metricWithHeader = new MetricWithHeader(metricMessage.getMetricHeader(), metric);
                component.dispatchAll(Objects.hash(metric.getMetricName(), metric.getTags()), metricWithHeader);
            }
        }

    }

    @Override
    public String getDecodeType() {
        return "binaryToMetric";
    }
}
