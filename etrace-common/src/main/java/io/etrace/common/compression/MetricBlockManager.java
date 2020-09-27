package io.etrace.common.compression;

import java.io.IOException;

public class MetricBlockManager<K> extends BlockManager<K, MetricCompressor> {

    public MetricBlockManager(int flushThreshold, int flushInterval) {
        super(flushThreshold, flushInterval);
    }

    public MetricCompressor store(K key, byte[] data) throws IOException {
        MetricCompressor blockStore = blockStoreMap.computeIfAbsent(key, x -> new MetricCompressor());
        int blockSize = blockStore.store(data);
        if (blockSize >= flushThreshold || (System.currentTimeMillis() - blockStore.lastFlushTime()) >= flushInterval) {
            return blockStore;
        }
        return null;
    }

}
