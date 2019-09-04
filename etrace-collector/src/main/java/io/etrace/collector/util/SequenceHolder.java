package io.etrace.collector.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lizun
 *         Date: 17/2/22
 *         Time: 下午2:38
 */
public class SequenceHolder {

    private static SequenceHolder instance = null;
    private final Map<String, AtomicLong> groupBySequces = new ConcurrentHashMap<>();
    private final AtomicLong noAssignedSequences = new AtomicLong();

    private enum SequenceKey {
        GROUP_BY, TOPIC_NAME, PARTITION
    }

    private SequenceHolder() {
        clear();
    }

    public void clear() {
        groupBySequces.clear();
        noAssignedSequences.set(0);
    }

    public Long getNextIdByKey(String groupBy) {
        return groupBySequces.computeIfAbsent(groupBy, x -> new AtomicLong()).incrementAndGet();
    }

    public Long getNextIdNoAssigned() {
        return noAssignedSequences.incrementAndGet();
    }

    public static synchronized SequenceHolder getInstance() {
        if (null == instance) {
            synchronized (SequenceHolder.class) {
                if (null == instance) {
                    instance = new SequenceHolder();
                }
            }
        }
        return instance;
    }
}
