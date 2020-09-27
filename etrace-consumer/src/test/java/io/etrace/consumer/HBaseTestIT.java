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

package io.etrace.consumer;

import com.google.common.collect.Lists;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.util.IPUtil;
import io.etrace.common.util.RequestIdHelper;
import io.etrace.consumer.controller.CallStackController;
import io.etrace.consumer.model.BlockIndex;
import io.etrace.consumer.service.HBaseStackDao;
import io.etrace.consumer.storage.hbase.HBaseClient;
import io.etrace.consumer.storage.hbase.HBaseClientFactory;
import io.etrace.consumer.storage.hbase.PutBuilder;
import io.etrace.consumer.storage.hbase.StackTable;
import io.etrace.consumer.storage.hbase.impl.StackImpl;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HBaseTestIT {

    @Autowired
    private HBaseClient hBaseClient;
    @Autowired
    private StackTable stackSchema;
    @Autowired
    private HBaseStackDao hBaseStackDao;
    @Autowired
    private HBaseClientFactory hBaseClientFactory;

    @Autowired
    private CallStackController callStackController;
    @Autowired
    private StackImpl stack;

    @Test
    public void writeAndRead() throws IOException {
        String requestId = "one_request_id";
        String rpcId = "1.1";
        int ts = 6001;

        // write
        short shard = hBaseClientFactory.getShardId(ts, RequestIdHelper.getRequestId(requestId).hashCode());
        Put put = PutBuilder.createPut(PutBuilder.createRowKey(shard, requestId), ts);
        CallStackV1 callstack = new CallStackV1();
        callstack.setId(rpcId);

        byte[] qualifierValue = stackSchema.buildQualifierValue(callstack, 22, 11, 3,
            IPUtil.ipToLong("127.0.0.1"), (short)4);
        put.addColumn(stackSchema.getCf(), Bytes.toBytes(callstack.getId()), qualifierValue);
        hBaseClient.executeBatch(stack.getName(), 1, Lists.newArrayList(put));
        // query
        BlockIndex result = hBaseStackDao.findBlockIndex(requestId, rpcId, ts);

        //System.out.println(result);

        assertEquals(22, result.getOffset());
        assertEquals(11, result.getBlockId());
        assertEquals(3, result.getHour());
        assertEquals("127.0.0.1", result.getIp());
        assertEquals(4, result.getIndex());
    }
}
