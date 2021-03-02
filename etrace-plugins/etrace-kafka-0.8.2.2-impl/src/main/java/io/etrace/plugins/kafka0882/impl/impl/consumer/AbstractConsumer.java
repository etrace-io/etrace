/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.plugins.kafka0882.impl.impl.consumer;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etrace.common.channel.KafkaConsumerProp;
import io.etrace.common.util.JSONUtil;
import io.etrace.plugins.kafka0882.KafkaMessageListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.etrace.plugins.kafka0882.impl.impl.metrics.MetricName.*;

public abstract class AbstractConsumer implements KafkaMessageListener<MessageAndMetadata<byte[], byte[]>> {
    public final Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

    private ConsumerConnector consumer;
    private ExecutorService executor;
    private List<KafkaStream<byte[], byte[]>> streams;
    private final String name;
    private volatile boolean running = false;
    //private Counter count;
    //private Counter payload;
    private final Map<String, Counter> counterMap = Maps.newHashMap();
    private final Map<String, Counter> payloadMap = Maps.newHashMap();
    private final Map<String, Timer> latencyMap = Maps.newHashMap();

    public AbstractConsumer(String name) {
        this.name = name;
    }

    public static Properties getKafkaConsumerCommonProp() {
        Properties consumerProperty = new Properties();
        consumerProperty.put("fetch.message.max.bytes", "10485760");
        consumerProperty.put("socket.receive.buffer.bytes", "409600");
        consumerProperty.put("num.consumer.fetchers", "2");
        consumerProperty.put("queued.max.message.chunks", "16");
        //避免gc时间过长,触发session超时,导致rebalance
        consumerProperty.put("rebalance.backoff.ms", "3000");
        consumerProperty.put("rebalance.max.retries", "5");
        consumerProperty.put("zookeeper.session.timeout.ms", "12000");
        return consumerProperty;
    }

    public void startup(Map<String, String> props, KafkaConsumerProp consumerProp) {
        Properties consumerConfig = getKafkaConsumerCommonProp();
        consumerConfig.putAll(props);
        consumerConfig.put("group.id", consumerProp.getGroup());

        ConsumerConfig config = new ConsumerConfig(consumerConfig);
        consumer = Consumer.createJavaConsumerConnector(config);
        TopicFilter topicFilter = new Whitelist(consumerProp.getTopics());

        streams = consumer.createMessageStreamsByFilter(topicFilter);
        executor = new ThreadPoolExecutor(streams.size(), streams.size(), 0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("kafka_" + consumerProp.getTopics() + "_%d").build(),
            new ThreadPoolExecutor.AbortPolicy());

        //metrics
        //count = Metrics.counter(KAFKA_CONSUMER, Tags.of("topic", consumerProp.getTopics()));
        //payload = Metrics.counter(KAFKA_THROUGHPUT, Tags.of("topic", consumerProp.getTopics()));

        running = true;

        work();
    }

    private void work() {
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            executor.submit(() -> {
                ConsumerIterator iterator = stream.iterator();
                while (running) {
                    MessageAndMetadata<byte[], byte[]> message = iterator.next();
                    Counter counter = counterMap.computeIfAbsent(message.topic(), topic ->
                        Metrics.counter(KAFKA_CONSUMER, Tags.of("topic", topic)));
                    Counter payload = payloadMap.computeIfAbsent(message.topic(), topic ->
                        Metrics.counter(KAFKA_THROUGHPUT, Tags.of("topic", topic)));
                    Timer latency = latencyMap.computeIfAbsent(message.topic(), topic ->
                        Metrics.timer(KAFKA_LATENCY, Tags.of("topic", topic)));
                    try {
                        Map<String, Object> header = JSONUtil.toObject(message.key(), Map.class);
                        long timestamp = (long)header.getOrDefault("timestamp", 0L);
                        latency.record(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        LOGGER.error("fail to parse Message Header for topic[{}].", message.topic(), e);
                    }

                    try {
                        onMessage(message);
                    } catch (Exception e) {
                        LOGGER.error("consumer kafka message error:", e);
                    } finally {
                        counter.increment();
                        payload.increment(message.key().length + message.message().length);
                    }
                }
            });
        }
    }

    @Override
    public void destroy() {
        if (consumer != null) {
            consumer.commitOffsets(true);
            consumer.shutdown();
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
                for (int i = 0; i < 1000; i++) {
                    if (!executor.awaitTermination(1, TimeUnit.MILLISECONDS)) {
                        running = false;
                        //                        System.out.println("Timed out waiting for consumer threads to shut
                        //                        down, exiting uncleanly. id:" + i);
                        executor.shutdownNow();
                    }
                }
            } catch (InterruptedException ignore) {
                System.out.println("Interrupted during shutdown, exiting uncleanly. ".concat(name));
            } finally {
                running = false;
                executor.shutdownNow();
            }
        }
    }

    @Override
    public String name() {
        return name;
    }
}
