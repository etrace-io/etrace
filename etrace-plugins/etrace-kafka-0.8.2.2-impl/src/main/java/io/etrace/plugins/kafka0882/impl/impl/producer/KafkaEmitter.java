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
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import io.etrace.plugins.kafka0882.impl.impl.producer.sink.KafkaSlaveProducer;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KafkaEmitter {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaEmitter.class);

    private static KafkaManager kafkaManager = KafkaManager.getInstance();
    private static int maxRetryCount = 10;

    public static boolean send(RecordInfo record) throws Exception {
        KafkaCluster kafkaCluster = getKafkaCluster(record);
        if (null == kafkaCluster) {
            return false;
        }

        KafkaSlaveProducer sink = kafkaCluster.getOrCreateProducer(record.getLeader());
        try {
            if (sink.send(record)) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("throw a uncaught exception", e);
        }

        return randomSend(record);
    }

    public static boolean randomSend(RecordInfo record) throws Exception {
        record.addRetry();

        KafkaCluster kafkaCluster = kafkaManager.getClusterByTopic(record.getRecord().topic());
        kafkaCluster.getNextPartition(record);
        return send(record);
    }

    private static KafkaCluster getKafkaCluster(RecordInfo record) throws KafkaEmitterException {
        if (record.getRetryCount() > maxRetryCount) {
            return null;
        }
        String topic = record.getRecord().topic();
        Integer partition = record.getRecord().partition();
        if (null == topic || topic.length() < 1) {
            throw new KafkaEmitterException("kafka topic must be not null!");
        }
        if (null == partition) {
            throw new KafkaEmitterException("kafka partition must be not null for topic:" + topic);
        }
        KafkaCluster cluster = kafkaManager.getClusterByTopic(topic);
        if (null == cluster) {
            throw new KafkaEmitterException((String.format("topic [ %s ] not found the kafka cluster", topic)));
        }
        return cluster;
    }

    public static List<PartitionInfo> getPartitionsByTopic(String topic) throws Exception {
        KafkaCluster kafkaCluster = kafkaManager.getClusterByTopic(topic);
        return kafkaCluster.availablePartitions(topic);
    }

    public static Partition getNextPartitionIdFor(String topic) throws Exception {
        return kafkaManager.getClusterByTopic(topic).getNextPartitionIdFor(topic);
    }
}
