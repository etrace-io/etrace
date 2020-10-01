package io.etrace.stream.container.exporter.kafka.model;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class RoundRobinStrategy implements HashStrategy {

    private ThreadLocal<Map<String, TopicSeq>> topicSeqThreadLocal;

    public RoundRobinStrategy() {
        topicSeqThreadLocal = ThreadLocal.withInitial(() -> newHashMap());
    }

    @Override
    public int hash(Object key, Object value) {
        Map<String, TopicSeq> topicSeq = topicSeqThreadLocal.get();
        TopicSeq seq = topicSeq.computeIfAbsent(key.toString(), t -> new TopicSeq());
        int hash = seq.seq++;
        if (hash == Integer.MIN_VALUE) {
            hash = 0;
        }
        return hash;
    }
}
