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

import io.etrace.common.message.trace.CallStackV1;
import io.etrace.consumer.model.BlockIndex;

public abstract class StackTable implements TableSchema {

    /**
     * qualifierValue: hour + ip + idx + block id + message offset
     */
    public abstract byte[] buildQualifierValue(CallStackV1 callStack, int messageOffset, long blockId, long hour,
                                               long ip,
                                               short idx);

    public abstract BlockIndex decode(byte[] data);

    @Override
    public String getName() {
        return "stack";
    }

    @Override
    public byte[] getCf() {
        return "t".getBytes();
    }
}
