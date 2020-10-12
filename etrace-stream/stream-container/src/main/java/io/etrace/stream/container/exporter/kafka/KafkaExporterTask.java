package io.etrace.stream.container.exporter.kafka;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import io.etrace.common.HeaderKey;
import io.etrace.common.compression.MetricBlockManager;
import io.etrace.common.compression.MetricCompressor;
import io.etrace.common.constant.InternalMetricName;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.codec.FramedMetricMessageCodec;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Exporter;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.common.util.JSONUtil;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaCluster;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaEmitter;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaManager;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import io.etrace.stream.container.exporter.kafka.model.HashFactory;
import io.etrace.stream.container.exporter.kafka.model.HashStrategy;
import io.etrace.stream.container.service.ChannelManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static io.etrace.common.constant.InternalMetricName.KAFKA_PRODUCER_LOSE_DATA;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KafkaExporterTask extends DefaultSyncTask implements Exporter {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaExporterTask.class);
    private final Counter loseDataCounter;
    @Autowired
    ChannelManager channelManager;
    private FramedMetricMessageCodec metricMessageCodec = new FramedMetricMessageCodec();
    private String source;
    private HashStrategy hashStrategy;
    private Map<String, Counter> blockToSend = new ConcurrentHashMap<>();
    private Map<String, Counter> throughput = new ConcurrentHashMap<>();
    private Map<String, Counter> metricToSend = new ConcurrentHashMap<>();
    private String resourceId;
    private Resource resource;
    private ThreadLocal<MetricBlockManager> blockStoreManagerThreadLocal;
    // hold blockManager to flushAll when terminate
    private List<MetricBlockManager> blockStoreManagerList = Collections.synchronizedList(newArrayList());

    public KafkaExporterTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        Object shardingType = Optional.ofNullable(params.get("shardingStrategy")).orElseGet(
            (Supplier<Object>)() -> HashFactory.HashType.HASHING.name());
        hashStrategy = HashFactory.newInstance(shardingType.toString());
        source = params.get("source").toString();

        resourceId = String.valueOf(params.get("resourceId"));
        int blockSize = (int)Optional.ofNullable(params.get("blockSize")).orElse(64 * 1024);
        int flushInterval = (int)Optional.ofNullable(params.get("flushInterval")).orElse(4000);

        blockStoreManagerThreadLocal = ThreadLocal.withInitial(() -> {
            MetricBlockManager<Partition> blockStoreManager = new MetricBlockManager<>(blockSize, flushInterval);
            blockStoreManagerList.add(blockStoreManager);
            return blockStoreManager;
        });

        loseDataCounter = Counter.builder(KAFKA_PRODUCER_LOSE_DATA)
            .tag("pipeline", component.getPipeline())
            .register(Metrics.globalRegistry);
    }

    static List<List<Metric>> grouping(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyList();
        }
        final int size = 100;
        List<List<Metric>> groups = newArrayList();
        List<Metric> group = newArrayListWithCapacity(size);
        for (Metric m : metrics) {
            group.add(m);
            if (group.size() == size) {
                groups.add(group);
                group = newArrayListWithCapacity(size);
            }
        }
        if (!group.isEmpty()) {
            groups.add(group);
        }
        return groups;
    }

    @Override
    public void init(Object... param) {
        List<Resource> resources = (List<Resource>)param[1];
        resource = resources.stream().filter((Predicate<Resource>)r -> r.getName().equals(resourceId)).findFirst()
            .get();
    }

    @Override
    public void startup() {
        super.startup();

        KafkaCluster kafkaCluster = channelManager.createCluster(resourceId, resource);

        KafkaManager.getInstance().register(source, kafkaCluster);
    }

    @Override
    public void stop() {
        super.stop();
    }

    private Counter getMetricCounter(Map<String, Counter> counterMap, String topic, String metricName) {
        Counter counter = counterMap.get(topic);
        if (counter == null) {
            counter = Counter.builder(metricName)
                .tag("pipeline", component.getPipeline())
                .tag("name", component.getName())
                .tag("topic", topic)
                .register(Metrics.globalRegistry);
            counterMap.put(topic, counter);
        }
        return counter;
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        if (event instanceof Collection) {
            Collection<Metric> collection = (Collection<Metric>)event;
            handleMetrics(collection);
        } else if (event instanceof Metric) {
            throw new RuntimeException("error to be here when input the Metric !!!");
        }
    }

    private void handleMetrics(Collection<Metric> metrics) {
        Map<Partition, List<Metric>> metricGroups = newHashMap();
        for (Metric metric : metrics) {
            if (metric.getMetricName().equals("stream.esper.check.flush.event")) {
                // CheckFlushEvent has no source
                // common.epl
                flush(blockStoreManagerThreadLocal.get().getBlocksIfNeedFlush(false));
                continue;
            }
            //            if (SwitchManager.kafkaLoseDataSwitch) {
            //                loseDataCounter.increment();
            //                continue;
            //            }

            if (metric.getSource() == null) {
                throw new RuntimeException("metric source is null!");
            }

            try {
                int hash = hashStrategy.hash(source, metric);
                List<PartitionInfo> partitionInfos = KafkaEmitter.getPartitionsByTopic(source);

                PartitionInfo partitionInfo = partitionInfos.get(Math.abs(hash) % partitionInfos.size());
                Partition partition = new Partition(partitionInfo.partition(), partitionInfo.leader().id());

                List<Metric> metricList = metricGroups.computeIfAbsent(partition, t -> newArrayList());
                metricList.add(metric);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        if (metricGroups.size() <= 0) {
            return;
        }

        MetricBlockManager<Partition> blockStoreManager = blockStoreManagerThreadLocal.get();
        try {
            for (Map.Entry<Partition, List<Metric>> entry : metricGroups.entrySet()) {
                MetricMessage metricMessage = new MetricMessage();
                List<List<Metric>> groups = grouping(entry.getValue());
                for (List<Metric> group : groups) {
                    metricMessage.setMetrics(group);
                    byte[] msg = metricMessageCodec.encode(metricMessage);
                    MetricCompressor metricCompressor = blockStoreManager.store(entry.getKey(), msg);
                    if (null != metricCompressor) {
                        sendToKafka(entry.getKey(), metricCompressor.flush());
                    }
                }
                getMetricCounter(metricToSend, source, InternalMetricName.KAFKA_PRODUCER_METRIC_SEND).increment(
                    entry.getValue().size());
            }

            flush(blockStoreManager.getBlocksIfNeedFlush(false));
        } catch (Exception ex) {
            throw new RuntimeException("send to kafka error", ex);
        }

    }

    private void flush(Map<Partition, MetricCompressor> blocks) {
        if (blocks.size() > 0) {
            blocks.entrySet().forEach(c -> {
                try {
                    sendToKafka(c.getKey(), c.getValue().flush());
                } catch (Exception e) {
                    LOGGER.error("trace send error:", e);
                }
            });
        }
    }

    private void sendToKafka(Partition partition, byte[] data) {
        if (null == data) {
            return;
        }
        Counter throughputCounter = getMetricCounter(throughput, source, InternalMetricName.KAFKA_PRODUCER_THROUGHPUT);

        try {
            Map<String, Object> key = new HashMap<>();
            key.put("timestamp", System.currentTimeMillis());
            byte[] keyBytes = JSONUtil.toJsonAsBytes(key);
            throughputCounter.increment(keyBytes.length);

            RecordInfo recordInfo = new RecordInfo(partition.getLeader(), null,
                new ProducerRecord(source, partition.getPartition(),
                    JSONUtil.toBytes(new HeaderKey(System.currentTimeMillis())), data));
            //            LOGGER.info("topic["+source+"]:"+new String(data));

            if (!KafkaEmitter.send(recordInfo)) {
                try {
                    String msg = "send to kafka <" + JSONUtil.toJson(partition) + "> failed";
                    LOGGER.error(msg);
                    //                setMsg(msg);
                } catch (IOException ex) {
                    LOGGER.error("", ex);
                    //                setThrowable(ex);
                }
                return;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        throughputCounter.increment(data.length);
        getMetricCounter(blockToSend, source, InternalMetricName.KAFKA_BLOCK_STORE_SEND).increment();
    }

}
