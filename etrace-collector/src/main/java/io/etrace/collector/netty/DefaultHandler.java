package io.etrace.collector.netty;

import io.etrace.agent.Trace;
import io.etrace.collector.service.Pipeline;
import io.etrace.collector.netty.thrift.ThriftHandler;
import io.etrace.collector.service.BalanceThroughputService;
import io.etrace.collector.service.ForbiddenConfigService;
import io.etrace.collector.service.TrueMetricsService;
import io.etrace.common.Constants;
import io.etrace.common.modal.MessageHeader;
import io.etrace.common.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Maps.newHashMap;

@Component
public class DefaultHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(ThriftHandler.class);
    @Autowired
    private Pipeline pipeline;
    @Autowired
    private ForbiddenConfigService forbiddenConfigService;
    @Autowired
    private BalanceThroughputService balanceThroughputService;

    @Autowired
    TrueMetricsService trueMetricsService;

    private long errorCount = 0;

    public void process(byte[] header, byte[] body, String serverType) {
        String messageType;
        try {
            long start = System.currentTimeMillis();
            if (header == null || body == null) {
                return;
            }

            MessageHeader messageHeader = JSONUtil.toObject(header, MessageHeader.class);
            if (messageHeader == null) {
                Trace.logEvent("header", "null", "0", new String(header), null);
                return;
            }

            messageType = messageHeader.getMessageType();

            // hard code禁止esight
            // 原因是,如果直接在router配置上删除esight
            // 则dal的基于appid的Router仍然会受到dal发送的esight数据, 导致解析异常(用CallstackCodec解析eSight)
            // 虽然对业务无多大影响, 但为了防止未来代码意外修改
            // 让一些其他AppIdRouter里的业务使用了esight
            // 所以在此处源头需要禁止
            if (Objects.nonNull(messageType) && "esight".equals(messageType.toLowerCase())) {
                return;
            }

            int size = header.length + body.length;

            trueMetricsService.defaultHandlerThroughput(size);

            this.balanceThroughputService.update(size);

            if (messageHeader.getAppId() != null && messageType == null) {
                //for old trace protocol version.
                messageHeader.setMessageType("trace");
            }
            if (messageHeader.getAppId() != null) {
                String appId = messageHeader.getAppId();

                if (forbiddenConfigService.isForbiddenAppId(appId)) {
                    trueMetricsService.defaultHandlerforbiddenThoughPut(appId, size);
                    return;
                }

                trueMetricsService.agentThoughPut(messageHeader.getAppId(), messageHeader.getMessageType(), size);

                if (messageHeader.getAst() >= 0) {
                    trueMetricsService.agentLatency(appId, start - messageHeader.getAst(), messageType, serverType);
                }

                int frameSize = 1024 * 1024;
                int maxFrameSize = frameSize * 8;
                if (size > maxFrameSize) {
                    int mod = size / frameSize;
                    Map<String, String> tags = newHashMap();
                    tags.put("hostName", messageHeader.getHostIp());
                    tags.put("instance", messageHeader.getInstance());
                    tags.put("key", messageHeader.getKey());
                    tags.put("messageType", messageHeader.getMessageType());
                    tags.put("dalGroup", messageHeader.getDalGroup());
                    tags.put("appId", messageHeader.getAppId());
                    tags.put("hostIp", messageHeader.getHostIp());
                    tags.put("agentSendTime", String.valueOf(messageHeader.getAst()));

                    Trace.logEvent("Too-Long-Package-" + mod, appId, Constants.SUCCESS, tags);
                }
            }
            messageHeader.setCrt(start);

            pipeline.produce(messageHeader, body);
        } catch (Exception e) {

            Trace.logError("[" + serverType + "]" + "Process message error," + errorCount, e);

            trueMetricsService.defaultHandlerError(1);

            if (errorCount % 10 == 0) {
                LOGGER.error("[" + serverType + "]" + "Produce message into memory queue error:" + errorCount, e);
            }
            errorCount++;
        }
    }

}
