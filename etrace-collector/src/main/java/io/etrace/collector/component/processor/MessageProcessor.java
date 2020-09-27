package io.etrace.collector.component.processor;

import com.google.common.base.Strings;
import io.etrace.collector.metrics.MetricsService;
import io.etrace.collector.service.CollectorConfigurationService;
import io.etrace.collector.sharding.impl.FrontShardIngImpl;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.common.util.Bytes;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.Map;

import static io.etrace.collector.metrics.MetricName.THROUGHPUT;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageProcessor extends DefaultSyncTask {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    @Autowired
    public FrontShardIngImpl balanceThroughputService;
    @Autowired
    public CollectorConfigurationService collectorConfigurationService;
    @Autowired
    public MetricsService metricsService;

    public Counter throughput;

    public MessageProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        Tags tags = Tags.of("host", NetworkInterfaceHelper.INSTANCE.getLocalHostName());
        throughput = Metrics.counter(THROUGHPUT, tags);
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        byte[] header = (byte[])key;
        byte[] body = (byte[])event;

        String messageType = null;
        String appId = null;
        try {
            long current = System.currentTimeMillis();
            MessageHeader messageHeader = JSONUtil.toObject(header, MessageHeader.class);
            if (null == messageHeader) {
                LOGGER.warn("header deserialization exception. header:{}",
                    new String(header, 0, Math.min(1024, header.length), Bytes.UTF8_CHARSET));

                return;
            }

            int size = header.length + body.length;
            throughput.increment(size);
            balanceThroughputService.add(size);

            messageType = messageHeader.getMessageType();
            appId = messageHeader.getAppId();
            if (!Strings.isNullOrEmpty(appId)) {
                if (collectorConfigurationService.isForbiddenAppId(appId)) {
                    metricsService.forbiddenThoughPut(appId, size);
                    return;
                }
                messageType = Strings.isNullOrEmpty(messageType) ? "trace" : messageType;
                messageHeader.setMessageType(messageType);

                metricsService.agentThoughPut(appId, messageType, size);
                if (messageHeader.getAst() >= 0) {
                    metricsService.agentLatency(appId, current - messageHeader.getAst(), messageType);
                }
            }
            messageHeader.setCrt(current);
            component.routeToFirstComponent(messageHeader, body);
        } catch (Throwable e) {
            LOGGER.error("process agent:[{}] message type:[{}] error", appId, messageType, e);
        }
    }
}
