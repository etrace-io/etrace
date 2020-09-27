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

package io.etrace.consumer.storage.hbase;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class PutBuilder {
    private static final int STACK_DATA_LEN = 3 * Bytes.SIZEOF_LONG + Bytes.SIZEOF_INT + Bytes.SIZEOF_SHORT;

    /**
     * Create a Put operation for the specified row, using a given timestamp, and an existing row lock.
     *
     * @param row row key
     * @param ts  timestamp
     */
    public static Put createPut(byte[] row, long ts) {
        Put put = new Put(row, ts);
        put.setDurability(Durability.SKIP_WAL);
        return put;
    }

    public static byte[] createRowKey(short shard, String key) {
        byte[] keyData = Bytes.toBytes(key);
        byte[] rowKey = new byte[2 + keyData.length];
        int offset = Bytes.putShort(rowKey, 0, shard);
        Bytes.putBytes(rowKey, offset, keyData, 0, keyData.length);
        return rowKey;
    }

}
