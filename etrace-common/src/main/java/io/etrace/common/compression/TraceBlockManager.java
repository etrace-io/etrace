package io.etrace.common.compression;

import java.io.IOException;

public class TraceBlockManager<K> extends BlockManager<K, TraceCompressor> {

    public TraceBlockManager(int flushThreshold, int flushInterval) {
        super(flushThreshold, flushInterval);
    }

    public TraceCompressor store(K key, byte[] data, String instance) throws IOException {
        TraceCompressor blockStore = blockStoreMap.computeIfAbsent(key, x -> new TraceCompressor());
        int blockSize = blockStore.store(data, instance);
        if (blockSize >= flushThreshold || (System.currentTimeMillis() - blockStore.lastFlushTime()) >= flushInterval) {
            return blockStore;
        }
        return null;
    }

}
