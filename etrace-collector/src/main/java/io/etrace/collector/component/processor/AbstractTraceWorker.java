package io.etrace.collector.component.processor;

import io.etrace.collector.component.DiskBackedInMemoryTask;
import io.etrace.common.compression.CompressType;
import io.etrace.common.compression.TraceBlockManager;
import io.etrace.common.io.CallStackHeader;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Component;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.Pair;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class AbstractTraceWorker extends DiskBackedInMemoryTask {
    public static final int FLUSH_INTERVAL = 3 * 1000;
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTraceWorker.class);
    private final CompressType compressType;
    private TraceBlockManager blockManager;
    private String topic;

    public AbstractTraceWorker(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        this.compressType = CompressType.snappy;
        this.topic = params.get("topic").toString();
        this.blockManager = new TraceBlockManager((Integer)Optional.of(params.get("flushThreshold")).get(),
            FLUSH_INTERVAL);
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void process(Pair<MessageHeader, byte[]> pair) throws Exception {
        byte[] body = pair.getValue();
        Partition partition = getPartition(pair.getKey(), body);
        if (null == partition) {
            LOGGER.warn("AppId[{}] does not match the available partition!", pair.getKey().getAppId());
            return;
        }

        // todo: 原版使用了将 byte[] 解压成 List<byte[]> (List<Callstack>)，来做打包发送KAFKA。 大流量场景更高效
        sendToKafka(partition, pair.getValue());
    }

    private void sendToKafka(Partition partition, byte[] data) {
        try {
            if (null != data) {
                // todo: 验证 emptylist emptymap 对 消费时处理的影响； 若不需要这两个字段，更新header字段
                CallStackHeader header = new CallStackHeader(compressType.code(), Collections.emptyList(),
                    System.currentTimeMillis(), Collections.emptyMap());
                component.dispatchAll(partition.getLeader(),
                    new ProducerRecord<>(topic, partition.getPartition(), JSONUtil.toBytes(header), data));
            }
        } catch (Exception e) {
            LOGGER.error("send to kafka throw a exception:", e);
        }
    }

    //private void sendToKafka(Partition partition, TraceCompressor compressor) {
    //    try {
    //        byte[] data = compressor.flush();
    //        if (null != data) {
    //            CallStackHeader header = new CallStackHeader(compressType.code(), compressor.getOffsets(),
    //                System.currentTimeMillis(), compressor.getInstances());
    //
    //            component.dispatch(partition.getLeader(),
    //                new ProducerRecord<>(topic, partition.getPartition(), JSONUtil.toBytes(header), data));
    //
    //        }
    //    } catch (Exception e) {
    //        LOGGER.error("send to kafka throw a exception:", e);
    //    } finally {
    //        compressor.clear();
    //    }
    //}

    public abstract Partition getPartition(MessageHeader key, byte[] value) throws Exception;

    @Override
    public void checkIfNeedFlush() throws Exception {
        //flush(blockManager.getBlocksIfNeedFlush(false));
    }

    @Override
    protected void flush() {
        //flush(blockManager.getBlocksIfNeedFlush(true));
    }

    //private void flush(Map<Partition, TraceCompressor> blocks) {
    //    if (blocks.size() > 0) {
    //        blocks.entrySet().forEach(c -> {
    //            try {
    //                sendToKafka(c.getKey(), c.getValue());
    //            } catch (Exception e) {
    //                LOGGER.error("trace send error:", e);
    //            }
    //        });
    //    }
    //}

    public String getTopic() {
        return topic;
    }
}
