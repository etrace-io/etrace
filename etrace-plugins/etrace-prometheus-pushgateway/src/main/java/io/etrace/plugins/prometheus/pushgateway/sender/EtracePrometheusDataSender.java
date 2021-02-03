package io.etrace.plugins.prometheus.pushgateway.sender;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.message.metric.MetricHeaderV1;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.impl.AbstractMetric;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.util.JSONUtil;
import io.etrace.plugins.prometheus.pushgateway.convert.EtraceHelper;
import io.etrace.plugins.prometheus.pushgateway.model.EtraceExtendInfo;
import io.etrace.plugins.prometheus.pushgateway.model.EtraceMessage;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricV1;
import io.etrace.plugins.prometheus.pushgateway.network.CollectorSocket;
import io.etrace.plugins.prometheus.pushgateway.network.CollectorTcpAddressRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static io.etrace.plugins.prometheus.pushgateway.constants.PushGatewayConstants.PUSHGATEWAY_APPID;

@Component
public class EtracePrometheusDataSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtracePrometheusDataSender.class);

    @Value("${etrace.collector}")
    private String collectrAddress;

    @Value("${etrace.appId}")
    private String appId;

    private CollectorSocket collectorSocket;

    @PostConstruct
    public void startUp() {
        CollectorTcpAddressRegistry.build(collectrAddress, appId);
        collectorSocket = new CollectorSocket();
    }

    @PreDestroy
    public void shutdown() {
        collectorSocket.shutdown();
    }

    public boolean send(List<PrometheusMetricV1> prometheusMetricV1List, EtraceExtendInfo etraceExtendInfo) {
        List<AbstractMetric> metricList = EtraceHelper.convertPrometheusMetricToEtraceMetric(prometheusMetricV1List);
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(PUSHGATEWAY_APPID);
        messageHeader.setHostIp(etraceExtendInfo.getRemoteHostIp());
        messageHeader.setHostName(etraceExtendInfo.getRemoteHostName());
        messageHeader.setMessageType("Metric");
        messageHeader.setAst(System.currentTimeMillis());
        /**
         * 设置metric header信息
         */
        MetricHeaderV1 metricHeaderV1 = new MetricHeaderV1();
        metricHeaderV1.setAppId(PUSHGATEWAY_APPID);
        metricHeaderV1.setHostName(etraceExtendInfo.getRemoteHostName());
        metricHeaderV1.setHostIp(etraceExtendInfo.getRemoteHostIp());
        EtraceMessage etraceMessage = new EtraceMessage(messageHeader, metricHeaderV1, metricList);
        return sendData(etraceMessage);
    }

    private boolean sendData(EtraceMessage message) {
        if (null == message) {
            return false;
        }
        try {
            MessageHeader messageHeader = message.getMessageHeader();
            MetricHeaderV1 metricHeaderV1 = message.getMetricHeaderV1();
            List<AbstractMetric> metricList = message.getMetricList();
            byte[] messageHeaderBytes = JSONUtil.toBytes(messageHeader);
            byte[] messageBodyBytes = null;
            JsonFactory jsonFactory = new JsonFactory();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonGenerator generator = jsonFactory.createGenerator(baos, JsonEncoding.UTF8);
            generator.writeStartArray();
            wirteBody(generator, metricHeaderV1, metricList);
            generator.writeEndArray();
            generator.flush();
            if (baos != null && baos.size() > 0) {
                messageBodyBytes = baos.toByteArray();
                baos.reset();
            }
            return collectorSocket.send(messageHeaderBytes, messageBodyBytes);
        } catch (Exception e) {
            LOGGER.error("consumer message error", e);
        } finally {
            collectorSocket.tryCloseConnWhenLongTime();
        }
        return false;
    }

    public void wirteBody(JsonGenerator generator, MetricHeaderV1 metricHeaderV1, List<AbstractMetric> metricList)
        throws IOException {
        generator.writeStartArray();
        generator.writeString(JSONCodecV1.METRIC_PREFIX_V1);
        generator.writeString(AgentConfiguration.getTenant());
        generator.writeString(AgentConfiguration.getAppId());
        generator.writeString(metricHeaderV1.getHostIp());
        generator.writeString(metricHeaderV1.getHostName());
        generator.writeObject(null);
        generator.writeStartArray();
        if (!CollectionUtils.isEmpty(metricList)) {
            Iterator<AbstractMetric> metricIterator = metricList.iterator();
            while (metricIterator.hasNext()) {
                MetricInTraceApi<?> metricInTraceApi = metricIterator.next();
                if (metricInTraceApi == null) {
                    metricIterator.remove();
                    continue;
                }
                generator.writeStartArray();
                metricInTraceApi.write(generator);
                generator.writeEndArray();
                metricIterator.remove();
            }
        }
        generator.writeEndArray();
        generator.writeEndArray();
    }
}
