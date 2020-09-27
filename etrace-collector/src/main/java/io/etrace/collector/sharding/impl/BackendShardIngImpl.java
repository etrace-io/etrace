package io.etrace.collector.sharding.impl;

import com.google.common.hash.Hashing;
import io.etrace.collector.config.CollectorProperties;
import io.etrace.collector.sharding.BackendShardIng;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaEmitter;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Component
public class BackendShardIngImpl implements BackendShardIng {

    @Autowired
    private CollectorProperties collectorProperties;

    private CollectorProperties.ShardingConfig shardingConfig;

    @PostConstruct
    public void startup() {
        shardingConfig = collectorProperties.getSharding().get("backend");
    }

    @Override
    public boolean isEnabled() {
        return shardingConfig.isEnabled();
    }

    @Override
    public Partition getPartition(Object object, byte[] body, String topic) throws Exception {
        if (!isEnabled()) {
            return KafkaEmitter.getNextPartitionIdFor(topic);
        }
        MessageHeader messageHeader = (MessageHeader)object;
        List<PartitionInfo> parts = KafkaEmitter.getPartitionsByTopic(topic);
        //        int idx = Hashing.consistentHash(key.getAppId().hashCode(), parts.size());
        int idx = Hashing.consistentHash(Objects.hash(messageHeader.getAppId() + messageHeader.getHostIp()),
            parts.size());
        PartitionInfo info = parts.get(idx);
        return new Partition(info.partition(), info.leader().id());
    }
}
