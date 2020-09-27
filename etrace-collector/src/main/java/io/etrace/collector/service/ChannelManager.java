package io.etrace.collector.service;

import io.etrace.common.pipeline.PipelineConfiguration;
import io.etrace.plugins.kafka0882.impl.impl.producer.ClusterBuilder;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class ChannelManager {
    private final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);

    private Map<String, Map<String, Closeable>> channels = new HashMap<>();

    @PreDestroy
    public void stop() {
        for (Map<String, Closeable> entry : channels.values()) {
            for (Closeable closeable : entry.values()) {
                try {
                    closeable.close();
                } catch (Throwable e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    public synchronized KafkaCluster initKafkaCluster(String name, Map<String, String> props) {
        Properties properties = new Properties();
        properties.putAll(props);
        KafkaCluster kafkaCluster = new ClusterBuilder().clusterName(name).producerConfig(properties).build();

        Map<String, Closeable> clusters = channels.computeIfAbsent(PipelineConfiguration.Channel.Type.KAFKA.toString(),
            k -> new HashMap<>());
        clusters.put(name, kafkaCluster);
        return kafkaCluster;
    }

}

