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

import org.apache.hadoop.hbase.util.Bytes;

public class PartitionRowKeyManager {

    private int partition = 60;

    public PartitionRowKeyManager() {
    }

    public PartitionRowKeyManager(int partition) {
        this.partition = partition;
    }

    public byte[][] calcSplitKeys() {
        byte[][] splitKeys = new byte[partition - 1][];
        for (short i = 1; i < partition; i++) {
            splitKeys[i - 1] = Bytes.toBytes(i);
        }
        return splitKeys;
    }

    public int getPartition() {
        return partition;
    }
}
