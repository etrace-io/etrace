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

package io.etrace.consumer.util;

import org.apache.hadoop.hbase.util.Bytes;

public class RowKeyUtil {

    private static int getLength(Object... parts) {
        int length = 0;
        for (int i = 0; i < parts.length; i++) {
            Object part = parts[i];
            if (part == null) {
                continue;
            }
            if (part instanceof Byte) {
                length += Bytes.SIZEOF_BYTE;
            } else if (part instanceof Short) {
                length += Bytes.SIZEOF_SHORT;
            } else if (part instanceof Integer) {
                length += Bytes.SIZEOF_INT;
            } else if (part instanceof Long) {
                length += Bytes.SIZEOF_LONG;
            } else if (part instanceof Float) {
                length += Bytes.SIZEOF_FLOAT;
            } else if (part instanceof String) {
                byte[] bytes = Bytes.toBytes((String)part);
                length += bytes.length;
                parts[i] = bytes;
            } else {
                length += ((byte[])part).length;
            }
        }
        return length;
    }

    public static byte[] build(Object... parts) {
        int length = getLength(parts);
        byte[] rowKey = new byte[length];
        int offset = 0;
        for (Object part : parts) {
            if (part == null) {
                continue;
            }
            if (part instanceof Byte) {
                offset = Bytes.putByte(rowKey, offset, (Byte)part);
            } else if (part instanceof Short) {
                offset = Bytes.putShort(rowKey, offset, (Short)part);
            } else if (part instanceof Integer) {
                offset = Bytes.putInt(rowKey, offset, (Integer)part);
            } else if (part instanceof Long) {
                offset = Bytes.putLong(rowKey, offset, (Long)part);
            } else if (part instanceof Float) {
                offset = Bytes.putFloat(rowKey, offset, (Float)part);
            } else {
                byte[] bytes = (byte[])part;
                offset = Bytes.putBytes(rowKey, offset, bytes, 0, bytes.length);
            }
        }
        return rowKey;
    }
}
