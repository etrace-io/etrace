package io.etrace.collector.component.processor;

import com.google.common.hash.Hashing;
import io.etrace.collector.component.DiskBackedInMemoryTask;
import io.etrace.collector.metrics.MetricsService;
import io.etrace.collector.worker.impl.KafkaCallback;
import io.etrace.common.HeaderKey;
import io.etrace.common.compression.MetricBlockManager;
import io.etrace.common.compression.MetricCompressor;
import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.codec.FramedMetricMessageCodec;
import io.etrace.common.pipeline.Component;
import io.etrace.common.util.JSONUtil;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaEmitter;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class AbstractMetricWorker extends DiskBackedInMemoryTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractMetricWorker.class);
    public MetricBlockManager blockManager;
    protected FramedMetricMessageCodec metricCodec = new FramedMetricMessageCodec();
    @Autowired
    private MetricsService metricsService;
    private String topic;

    public AbstractMetricWorker(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        this.topic = String.valueOf(params.get("topic"));
        this.blockManager = new MetricBlockManager(Integer.valueOf(String.valueOf(params.get("flushThreshold"))),
            FLUSH_INTERVAL);
    }

    @Override
    public void checkIfNeedFlush() throws Exception {
        flush(blockManager.getBlocksIfNeedFlush(false));
    }

    @Override
    protected void flush() {
        flush(blockManager.getBlocksIfNeedFlush(true));
    }

    private void flush(Map<Partition, MetricCompressor> blocks) {
        if (blocks.size() > 0) {
            blocks.entrySet().forEach(c -> {
                try {
                    send(c.getKey(), c.getValue().flush());
                } catch (Exception e) {
                    LOGGER.error("metric send error:", e);
                }
            });
        }
    }

    public void writeAndSend(List<MetricMessage> metricMessages) throws Exception {
        Map<Partition, List<MetricMessage>> messageListMap = writeMetrics(metricMessages);
        for (Map.Entry<Partition, List<MetricMessage>> entry : messageListMap.entrySet()) {
            for (MetricMessage metricMessage : entry.getValue()) {
                MetricCompressor metricCompressor = blockManager.store(entry.getKey(),
                    metricCodec.encode(metricMessage));
                if (null != metricCompressor) {
                    send(entry.getKey(), metricCompressor.flush());
                }
            }
        }
    }

    private void send(Partition partition, byte[] data) throws Exception {
        component.dispatchAll(partition.getLeader(), new ProducerRecord(topic, partition.getPartition(),
            JSONUtil.toBytes(new HeaderKey(System.currentTimeMillis())), data));
    }

    // todo: replace send with sendToKafka()
    public void sendToKafka(Partition partition, MetricCompressor compressor) throws Exception {
        KafkaEmitter.send(
            new RecordInfo(partition.getLeader(), new KafkaCallback(partition.getLeader(), metricsService),
                new ProducerRecord(topic, partition.getPartition(),
                    JSONUtil.toBytes(new HeaderKey(System.currentTimeMillis())), compressor.flush())));
    }

    public Map<Partition, List<MetricMessage>> writeMetrics(List<MetricMessage> metricMessageList) {
        Map<Partition, List<MetricMessage>> messageListMap = newHashMap();
        for (MetricMessage metricMessage : metricMessageList) {
            Map<Integer, MetricMessage> messageMap = newHashMap();

            metricMessage.forEach(pair -> {
                Integer key = pair.getValue().calcKey();
                MetricMessage currentMessage = messageMap.computeIfAbsent(key,
                    x -> new MetricMessage(pair.getKey(), newArrayList()));
                currentMessage.addMetric(pair.getValue());
            });
            messageMap.forEach((key, value) -> {
                try {
                    List<MetricMessage> messageList = messageListMap.computeIfAbsent(getPartitionWithHash(key),
                        x -> newArrayList());
                    messageList.add(value);
                } catch (Exception e) {
                    LOGGER.error("metrics aggregation compressor throw a exception:", e);
                }
            });
        }
        return messageListMap;
    }

    public Partition getPartitionWithHash(int hash) throws Exception {
        List<PartitionInfo> partitionInfoList = KafkaEmitter.getPartitionsByTopic(topic);
        int idx = Hashing.consistentHash(Math.abs(hash), partitionInfoList.size());
        PartitionInfo pInfo = partitionInfoList.get(idx);
        return new Partition(pInfo.partition(), pInfo.leader().id());
    }

    public String getTopic() {
        return topic;
    }
}
