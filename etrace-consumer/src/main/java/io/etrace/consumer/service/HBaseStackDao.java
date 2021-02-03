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

package io.etrace.consumer.service;

import io.etrace.common.message.trace.MessageId;
import io.etrace.common.util.RequestIdHelper;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.model.BlockIndex;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import io.etrace.consumer.storage.hbase.IHBaseTableNameFactory;
import io.etrace.consumer.storage.hbase.impl.StackImpl;
import io.etrace.consumer.util.RowKeyUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HBaseStackDao {

    @Autowired
    public IHBaseClientFactory ihBaseClientFactory;
    @Autowired
    public StackImpl stackImpl;
    @Autowired
    protected IHBaseTableNameFactory ihBaseTableNameFactory;

    /**
     * RowKey: shard + reqId
     * <p>
     * Column(t): key: rpcId 老版本是 Column(t): key: # + rpcId 2019-10-10
     * <p>
     * Column(t): value: hour + ip + index + blockOffset + messageOffset + appId + RPC
     */
    public BlockIndex findBlockIndex(MessageId messageId) throws IOException {
        return findBlockIndex(messageId.getRequestId(), messageId.getRpcId(), messageId.getTimestamp());
    }

    public BlockIndex findBlockIndex(String requestId, String rpcId, long msgTimestamp) throws IOException {
        byte[] rpcIdBytes = Bytes.toBytes(rpcId);

        HTable table = ihBaseClientFactory.getTableByPhysicalName(
            ihBaseTableNameFactory.getPhysicalTableNameByLogicalTableName(stackImpl.getLogicalTableName(),
                TimeHelper.getDay(msgTimestamp)));

        short shard = ihBaseClientFactory.getShardIdByPhysicalTableName(table.getName().getNameAsString(),
            RequestIdHelper.getRequestId(requestId).hashCode());
        try {
            byte[] rowKey = RowKeyUtil.build(shard, requestId);
            Get get = new Get(rowKey);
            get.addColumn(stackImpl.getColumnFamily(), rpcIdBytes);
            Result result = table.get(get);
            if (!result.isEmpty()) {
                byte[] data = result.getValue(stackImpl.getColumnFamily(), rpcIdBytes);
                return stackImpl.decode(data);
            }
        } finally {
            ihBaseClientFactory.closeHTable(table);
        }
        return null;

    }
}
