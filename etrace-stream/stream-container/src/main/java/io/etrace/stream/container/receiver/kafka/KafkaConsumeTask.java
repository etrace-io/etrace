package io.etrace.stream.container.receiver.kafka;

import com.google.common.collect.Maps;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.common.util.JSONUtil;
import io.etrace.stream.container.StreamContainer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Maps.newHashMap;
import static io.etrace.common.constant.InternalMetricName.*;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KafkaConsumeTask extends DefaultSyncTask {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumeTask.class);
    private final Map<String, Counter> throughputCounters = newHashMap();
    private final String resourceId;
    private ConsumerConnector consumer;
    private ExecutorService executor;
    private Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams;
    private Counter consumeError;
    private Timer pendingTimer;
    private Resource resource;
    private Map<String, Integer> topicAndFetchNums;

    public KafkaConsumeTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        this.resourceId = Optional.of(params.get("resourceId")).get().toString();

        topicAndFetchNums = Maps.newHashMap();
        String topicMap = params.get("topics").toString();
        Arrays.stream(topicMap.split(";")).forEach(pair -> {
            String[] arr = pair.split(",");
            topicAndFetchNums.put(arr[0], Integer.valueOf(arr[1]));
        });
    }

    static ConsumerConnector createKafkaConsumer(Resource resource, String pipeline) throws Exception {
        Properties props = new Properties();
        props.putAll(resource.getProps());
        props.put("group.id", pipeline);

        UUID uuid = UUID.randomUUID();
        props.put("consumer.id", String.format("%s-%s--%s--%s",
            InetAddress.getLocalHost().getHostName(),
            Optional.ofNullable(System.getProperty(StreamContainer.HTTP_PORT)).orElse("9000"),
            System.currentTimeMillis(),
            Long.toHexString(uuid.getMostSignificantBits()).substring(0, 8)));
        LOGGER.info(props.toString() + " =====");
        ConsumerConfig config = new ConsumerConfig(props);
        LOGGER.info("kafka consumer [{}] config properties: {}.", resource.getName(), config.props());
        return Consumer.createJavaConsumerConnector(config);
    }

    @Override
    public void init(Object... param) {
        List<Resource> resources = (List<Resource>)param[1];
        resource = resources.stream().filter(r -> r.getName().equals(resourceId)).findFirst().get();
    }

    @Override
    public void startup() {
        super.startup();
        try {
            consumer = createKafkaConsumer(resource, component.getPipeline());
            topicMessageStreams = consumer.createMessageStreams(topicAndFetchNums);

            for (String topic : topicAndFetchNums.keySet()) {
                Counter throughput = Counter.builder(KAFKA_CONSUME_THROUGHPUT)
                    .tag("pipeline", component.getPipeline())
                    .tag("dataSource", component.getName())
                    .tag("topic", topic)
                    .register(Metrics.globalRegistry);
                throughputCounters.put(topic, throughput);
            }

            consumeError = Counter.builder(KAFKA_CONSUME_ERROR)
                .tag("pipeline", component.getPipeline())
                .tag("dataSource", component.getName())
                .register(Metrics.globalRegistry);

            pendingTimer = Timer.builder(KAFKA_CONSUME_PENDING)
                .tag("pipeline", component.getPipeline())
                .tag("name", component.getName())
                .register(Metrics.globalRegistry);

            //create list of threads to consumer from each of the kafka partitions
            AtomicLong threadCounter = new AtomicLong();
            executor = Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName(
                    "KafkaDataSource-" + component.getPipeline() + "-" + component.getName() + "-" + threadCounter
                        .getAndIncrement());
                return thread;
            });

            consume();

            LOGGER.info("startup kafka datasource success for pipeline [{}],name [{}].", component.getPipeline(),
                component.getName());
        } catch (Exception e) {
            LOGGER.error("", e);
            System.exit(-1);
        }

    }

    private void consume() {
        if (topicMessageStreams == null || topicMessageStreams.isEmpty()) {
            LOGGER.warn("Consumer<name:{}, resource id:{}> topic stream is empty.", component.getName(), resourceId);
            return;
        }
        LOGGER.info("Start consumer [name:{},resource id:{}], topic stream size {}.", component.getName(), resourceId,
            topicMessageStreams.size());
        Set<Map.Entry<String, List<KafkaStream<byte[], byte[]>>>> entries = topicMessageStreams.entrySet();

        for (Map.Entry<String, List<KafkaStream<byte[], byte[]>>> entry : entries) {
            String topicName = entry.getKey();
            LOGGER.info("Consumer<name:{},resource id:{}> start consume kafka topic<{}>.", component.getName(),
                resourceId, topicName);
            List<KafkaStream<byte[], byte[]>> streams = entry.getValue();
            // consume the messages in the threads
            for (final KafkaStream<byte[], byte[]> stream : streams) {
                executor.submit(() -> {
                    try {
                        for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                            try {
                                byte[] message = msgAndMetadata.message();
                                if (message != null && message.length > 0) {
                                    long throughput = message.length;
                                    byte[] key = msgAndMetadata.key();
                                    if (key != null && key.length > 0) {
                                        throughput += key.length;
                                        Map<String, Object> messageHeader = JSONUtil.toObject(key, Map.class);
                                        if (messageHeader.containsKey("timestamp")) {
                                            long headerTimestamp = (long)messageHeader.get("timestamp");
                                            long now = System.currentTimeMillis();
                                            long latency = now - headerTimestamp;
                                            if (latency <= 0) {
                                                latency = 1;
                                            }
                                            pendingTimer.record(latency, TimeUnit.MILLISECONDS);
                                        }
                                    }

                                    String topic = msgAndMetadata.topic();
                                    component.dispatch(key, msgAndMetadata.message());

                                    Counter throughputCounter = throughputCounters.get(topic);
                                    if (throughputCounter != null) {
                                        throughputCounter.increment(throughput);
                                    }
                                }
                            } catch (Exception e) {
                                consumeError.increment();
                                //                                setThrowable(e);
                                LOGGER.error("", e);
                            }
                        }
                        LOGGER.info("kafka consumer thread [{}] exit", Thread.currentThread().getName());
                    } catch (Exception e) {
                        String msg = String.format(
                            "Consumer<name:%s,resource id:%s> consume error kafka topic<%s> error",
                            component.getName(), resourceId, topicName);
                        LOGGER.error(msg, e);
                        //                        setMsg(msg);
                        //                        setThrowable(e);
                    }
                });
            }
        }
    }

    @Override
    public void stop() {
        LOGGER.info("start to Shutdown kafka consumer[name:{},resource id:{}]....", component.getName(), resourceId);
        try {
            if (consumer != null) {
                consumer.commitOffsets(true);
                consumer.shutdown();
            }
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                        LOGGER.warn("Timeout waiting for consumer<name:{},resource id:{}> to shutdown.",
                            component.getName(), resourceId);
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    LOGGER.error("Interrupted during shutdown.", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Shutdown datasource error.", e);
        }
        LOGGER.info("Shutdown kafka consumer<name:{},resource id:{}> finished.", component.getName(), resourceId);

        super.stop();
    }

}
