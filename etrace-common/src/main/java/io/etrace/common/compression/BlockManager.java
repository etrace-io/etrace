package io.etrace.common.compression;

import com.google.common.collect.Maps;

import java.util.Map;

public class BlockManager<K, V extends Compressor> {
    int flushThreshold;
    int flushInterval;
    Map<K, V> blockStoreMap;

    public BlockManager(int flushThreshold, int flushInterval) {
        this.flushThreshold = flushThreshold * 1024;
        this.flushInterval = flushInterval;

        this.blockStoreMap = Maps.newHashMap();
    }

    public Map<K, V> getBlocksIfNeedFlush(boolean force) {
        Map<K, V> needFlush = Maps.newHashMap();
        for (Map.Entry<K, V> entry : blockStoreMap.entrySet()) {
            if (force) {
                if (entry.getValue().size() > 0) {
                    needFlush.put(entry.getKey(), entry.getValue());
                }
            } else if ((System.currentTimeMillis() - entry.getValue().lastFlushTime()) >= flushInterval
                || entry.getValue().size() >= flushThreshold) {
                needFlush.put(entry.getKey(), entry.getValue());
            }
        }
        return needFlush;
    }
}
