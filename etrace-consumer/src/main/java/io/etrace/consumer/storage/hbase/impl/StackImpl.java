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

package io.etrace.consumer.storage.hbase.impl;

import com.google.common.base.Strings;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.util.IPUtil;
import io.etrace.consumer.model.BlockIndex;
import io.etrace.consumer.storage.hbase.StackTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class StackImpl extends StackTable {

    public static final int STACK_DATA_LEN = 3 * Bytes.SIZEOF_LONG + Bytes.SIZEOF_INT + Bytes.SIZEOF_SHORT;

    @Override
    public byte[] buildQualifierValue(CallStackV1 callStack, int messageOffset, long blockId, long hour, long ip,
                                      short idx) {
        String appId = callStack.getAppId();
        if (Strings.isNullOrEmpty(appId)) {
            byte[] data = new byte[STACK_DATA_LEN];
            putData(data, 0, hour, ip, idx, blockId, messageOffset);
            return data;
        } else {
            byte[] appIdData = Bytes.toBytes(appId);
            byte[] data = new byte[STACK_DATA_LEN + Bytes.SIZEOF_SHORT + appIdData.length];
            int offset = putData(data, 0, hour, ip, idx, blockId, messageOffset);
            offset = Bytes.putShort(data, offset, (short)appIdData.length);
            Bytes.putBytes(data, offset, appIdData, 0, appIdData.length);
            return data;
        }
    }

    @Override
    public BlockIndex decode(byte[] data) {
        if (null == data || data.length < 1) {
            return null;
        }
        int pos = 0;
        long hour = Bytes.toLong(data, pos);
        pos += 8;
        String ip = IPUtil.longToIp(Bytes.toLong(data, pos));
        pos += 8;
        short idx = Bytes.toShort(data, pos);
        pos += 2;
        long blockOffset = Bytes.toLong(data, pos);
        pos += 8;
        int messageOffset = Bytes.toInt(data, pos);
        return new BlockIndex(hour, ip, idx, blockOffset, messageOffset);
    }

    public int putData(byte[] data, int offset, long hour, long ip, short idx, long blockOffset, int messageOffset) {
        offset = Bytes.putLong(data, offset, hour);
        offset = Bytes.putLong(data, offset, ip);
        offset = Bytes.putShort(data, offset, idx);
        offset = Bytes.putLong(data, offset, blockOffset);
        offset = Bytes.putInt(data, offset, messageOffset);
        return offset;
    }

}
