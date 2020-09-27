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

import com.google.common.collect.ImmutableList;
import io.etrace.plugins.kafka0882.impl.impl.IntObjectHashMap;
import io.etrace.plugins.kafka0882.impl.impl.producer.channel.ChannelManager;
import io.etrace.plugins.kafka0882.impl.impl.producer.exception.KafkaEmitterException;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import io.etrace.plugins.kafka0882.impl.impl.producer.sink.KafkaLeaderProducer;
import io.etrace.plugins.kafka0882.impl.impl.producer.sink.KafkaSlaveProducer;
import io.etrace.plugins.kafka0882.impl.impl.producer.sink.MetadataConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaCluster implements Closeable {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaCluster.class);
    private static Map<String, AtomicLong> partitionAutoId = new ConcurrentHashMap<>();
    private final String name;
    private Properties prop;
    private MetadataConfig metadataConfig;
    private KafkaLeaderProducer leaderProducer;
    private IntObjectHashMap<KafkaSlaveProducer> slaveProducers;
    private ChannelManager channelManager;

    public KafkaCluster(String name, Properties prop, MetadataConfig metadataConfig) {
        this.name = name;
        this.prop = prop;
        this.metadataConfig = metadataConfig;
        this.slaveProducers = new IntObjectHashMap<>(32);
        this.channelManager = new ChannelManager(this.metadataConfig.getChannelSize());

        this.leaderProducer = new KafkaLeaderProducer(this.name, initLeaderProp());
    }

    public KafkaSlaveProducer getOrCreateProducer(final int brokerId) {
        KafkaSlaveProducer slaveProducer = this.slaveProducers.get(brokerId);
        if (null == slaveProducer) {
            synchronized (KafkaCluster.class) {
                slaveProducer = this.slaveProducers.get(brokerId);
                if (null == slaveProducer) {
                    slaveProducer = new KafkaSlaveProducer(this, this.name, initSlaveProp(brokerId), brokerId,
                        channelManager.putMemoryChannel(brokerId));
                    slaveProducer.start();
                    this.slaveProducers.put(brokerId, slaveProducer);
                }
            }
        }
        return slaveProducer;
    }

    public void getNextPartition(RecordInfo record) throws Exception {
        Partition partition = getNextPartitionIdFor(record.getRecord().topic());
        ProducerRecord newRecord = new ProducerRecord(record.getRecord().topic(), partition.getPartition(),
            record.getRecord().key(), record.getRecord().value());

        record.setLeader(partition.getLeader());
        record.setRecord(newRecord);
    }

    public Partition getNextPartitionIdFor(String topic) throws Exception {
        AtomicLong seq = partitionAutoId.computeIfAbsent(topic, f -> new AtomicLong(0));
        List<PartitionInfo> partitions = availablePartitions(topic);

        if (null == partitions || partitions.size() < 1) {
            throw new KafkaEmitterException("Not available partitions for topic:[" + topic + "]");
        }

        int idx = (int)(seq.incrementAndGet() % partitions.size());

        PartitionInfo info = partitions.get(idx);
        return new Partition(info.partition(), info.leader().id());
    }

    public List<PartitionInfo> availablePartitions(String topic) throws Exception {
        return getParts(topic);
    }

    private List<PartitionInfo> getParts(String topic) throws Exception {
        List<PartitionInfo> partitions = leaderProducer.availablePartitionsFor(topic);
        List<Integer> ids = channelManager.getOverflowIds();

        if (ids.size() > 0) {
            ImmutableList.Builder<PartitionInfo> builder = new ImmutableList.Builder<>();
            for (PartitionInfo partitionInfo : partitions) {
                if (!ids.contains(partitionInfo.leader().id())) {
                    builder.add(partitionInfo);
                }
            }
            return builder.build();
        } else {
            return partitions;
        }
    }

    private Properties initLeaderProp() {
        Properties leaderProp = new Properties();
        leaderProp.putAll(prop);
        leaderProp.put(ProducerConfig.CLIENT_ID_CONFIG, name + "-leader");
        leaderProp.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, this.metadataConfig.getLeaderMetadataAge());
        leaderProp.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, this.metadataConfig.getLeaderReconnectMs());
        return leaderProp;
    }

    private Properties initSlaveProp(int brokerId) {
        Properties slaveProp = new Properties();
        slaveProp.putAll(prop);
        slaveProp.put(ProducerConfig.CLIENT_ID_CONFIG, name + "-producer-" + brokerId);
        slaveProp.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, this.metadataConfig.getSlaveMetadataAge());
        slaveProp.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, this.metadataConfig.getSlaveReconnectMs());
        return slaveProp;
    }

    @Override
    public void close() throws IOException {
        for (int i = 0; i < slaveProducers.size(); i++) {
            KafkaSlaveProducer producer = slaveProducers.get(i);
            if (null != producer) {
                try {
                    producer.close();
                } catch (Exception e) {
                    LOGGER.error("kafka cluster:[{}] brokerId:[{}] close throw a exception!", this.name, i, e);
                }
            }
        }
        leaderProducer.close();
    }
}
