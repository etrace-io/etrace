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

package io.etrace.consumer.model;

import org.apache.hadoop.hbase.util.Bytes;

public class ByteWrapper {
    private byte[] data;
    private int pos;
    private int limit;

    public ByteWrapper(byte[] data) {
        this(data, 0);
    }

    public ByteWrapper(byte[] data, int pos) {
        this.data = data;
        this.pos = pos;
        this.limit = data.length;
    }

    public void clear() {
        this.data = null;
    }

    public String getString() {
        short len = Bytes.toShort(data, pos);
        pos += Bytes.SIZEOF_SHORT;
        String result = Bytes.toString(data, pos, len);
        pos += len;
        return result;
    }

    public byte getByte() {
        byte result = data[pos];
        pos++;
        return result;
    }

    public short getShort() {
        short result = Bytes.toShort(data, pos);
        pos += Bytes.SIZEOF_SHORT;
        return result;
    }

    public int getInt() {
        int result = Bytes.toInt(data, pos);
        pos += Bytes.SIZEOF_INT;
        return result;
    }

    public long getLong() {
        long result = Bytes.toLong(data, pos);
        pos += Bytes.SIZEOF_LONG;
        return result;
    }

    public String getStringByLength(int length) {
        String result = Bytes.toString(data, pos, length);
        pos += length;
        return result;
    }

    public final boolean hasRemaining() {
        return pos < limit;
    }

    public void skipLength(int count) {
        pos += count;
    }

}
