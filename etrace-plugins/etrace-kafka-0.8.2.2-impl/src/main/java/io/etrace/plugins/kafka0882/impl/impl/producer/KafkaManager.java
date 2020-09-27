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

package io.etrace.plugins.kafka0882.impl.impl.producer;

import io.etrace.plugins.kafka0882.impl.impl.producer.exception.KafkaEmitterException;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaManager.class);
    private volatile static KafkaManager instance = null;
    private Map<String, KafkaCluster> mapping;

    private KafkaManager() {
        mapping = new ConcurrentHashMap<>();
    }

    public static KafkaManager getInstance() {
        if (null == instance) {
            synchronized (KafkaManager.class) {
                if (null == instance) {
                    instance = new KafkaManager();
                }
            }
        }
        return instance;
    }

    public boolean register(String topic, KafkaCluster kafkaCluster) {
        if (mapping.containsKey(topic)) {
            return false;
        }
        mapping.put(topic, kafkaCluster);
        return true;
    }

    KafkaCluster getClusterByTopic(String topic) {
        return mapping.get(topic);
    }

    public List<PartitionInfo> availablePartitions(String topic) throws Exception {
        KafkaCluster cluster = mapping.get(topic);
        if (null == cluster) {
            throw new KafkaEmitterException(String.format("topic [ %s ] not found the kafka cluster!", topic));
        }
        return cluster.availablePartitions(topic);
    }

    public void shutdown() {
        if (null != mapping) {
            for (KafkaCluster kafkaCluster : mapping.values()) {
                try {
                    kafkaCluster.close();
                } catch (IOException e) {
                    LOGGER.error("kafka cluster close throw a exception:", e);
                }
            }
        }
    }

}
