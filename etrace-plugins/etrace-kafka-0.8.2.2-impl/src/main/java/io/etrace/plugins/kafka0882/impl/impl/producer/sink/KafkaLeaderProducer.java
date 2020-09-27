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

package io.etrace.plugins.kafka0882.impl.impl.producer.sink;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.etrace.common.util.Pair;
import org.apache.kafka.common.PartitionInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaLeaderProducer extends KafkaSink {
    private static final int LEADER_ID = -1;

    private Map<String, Pair<List<PartitionInfo>, List<PartitionInfo>>> cacheTopicAndPartitions;

    public KafkaLeaderProducer(String cluster, Properties prop) {
        super(cluster, prop, LEADER_ID);

        this.cacheTopicAndPartitions = Maps.newConcurrentMap();
    }

    public List<PartitionInfo> availablePartitionsFor(String topic) throws Exception {
        List<PartitionInfo> partitionList = producer.partitionsFor(topic);
        Pair<List<PartitionInfo>, List<PartitionInfo>> pair = cacheTopicAndPartitions.get(topic);
        if (null != pair && pair.getKey() == partitionList) {
            return pair.getValue();
        }
        ImmutableList.Builder<PartitionInfo> builder = new ImmutableList.Builder<>();
        for (PartitionInfo part : partitionList) {
            if (part.leader() != null) {
                builder.add(part);
            }
        }
        List<PartitionInfo> availablePartitions = builder.build();
        cacheTopicAndPartitions.put(topic, new Pair<>(partitionList, availablePartitions));
        return availablePartitions;
    }

    @Override
    public void close() throws IOException {
        this.producer.close();
    }
}
