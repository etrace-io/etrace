package io.etrace.collector.component.processor;

import com.google.common.base.Strings;
import io.etrace.collector.metrics.MetricsService;
import io.etrace.collector.service.CollectorConfigurationService;
import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.codec.MetricCodecV1;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Processor;
import io.etrace.common.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MetricProcessor extends AbstractMetricWorker implements Processor {
    private final static String SPLIT_STR = "##";

    private MetricCodecV1 decoder = new MetricCodecV1();

    @Autowired
    private CollectorConfigurationService collectorConfigurationService;
    @Autowired
    private MetricsService metricsService;

    public MetricProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
    }

    private static String extractMetricName(String key) {
        int index = key == null ? -1 : key.indexOf(SPLIT_STR);
        String metricName;
        if (index < 0) {
            metricName = key;
        } else {
            metricName = key.substring(index + SPLIT_STR.length());
        }
        return metricName;
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void process(Pair<MessageHeader, byte[]> pair) throws Exception {
        if (Strings.isNullOrEmpty(pair.getKey().getAppId())) {
            return;
        }
        //todo key??
        String key = pair.getKey().getKey();
        String metricName = extractMetricName(key);
        if (collectorConfigurationService.isForbiddenMetricName(pair.getKey().getAppId(), metricName)) {
            metricsService.forbiddenMetrics(pair.getKey().getAppId(), pair.getValue().length);
            return;
        }

        // todo: 改成新版V1的格式即 MetricMessageV1
        List<MetricMessage> metricMessages = decoder.decodeToMetricMessageLegacyVersion(pair.getValue());
        writeAndSend(metricMessages);
    }

}
