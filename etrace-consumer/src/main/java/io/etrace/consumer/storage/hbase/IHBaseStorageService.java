package io.etrace.consumer.storage.hbase;

import org.apache.hadoop.hbase.client.Put;

public interface IHBaseStorageService {
    /**
     * Create a Put operation for the specified row, using a given timestamp, and an existing row lock.
     *
     * @param row row key
     * @param ts  timestamp
     */
    Put createPut(byte[] row, long ts);

    Put buildHbasePut(long timestamp, String requestId, short shard, byte[] columnFamily, byte[] qualifier,
                      byte[] qualifierValue);
}
