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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegexpBuilder {
    private final static String preStr = "(?s)";
    private final StringBuilder stringBuilder;

    public RegexpBuilder() {
        stringBuilder = new StringBuilder(preStr);
    }

    public RegexpBuilder append(byte b) {
        byte[] byteArray = {b};
        this.addBytes(stringBuilder, byteArray);
        return this;
    }

    public RegexpBuilder append(short s) {
        byte[] byteArray = Bytes.toBytes(s);
        this.addBytes(stringBuilder, byteArray);
        return this;
    }

    public RegexpBuilder append(int i) {
        byte[] byteArray = Bytes.toBytes(i);
        this.addBytes(stringBuilder, byteArray);
        return this;
    }

    public RegexpBuilder append(long l) {
        byte[] byteArray = Bytes.toBytes(l);
        this.addBytes(stringBuilder, byteArray);
        return this;
    }

    public RegexpBuilder append(byte[] bytes) {
        this.addBytes(stringBuilder, bytes);
        return this;
    }

    public RegexpBuilder skip(int length) {
        this.skip(stringBuilder, length);
        return this;
    }

    public RegexpBuilder skipByte() {
        this.skip(stringBuilder, Bytes.SIZEOF_BYTE);
        return this;
    }

    public RegexpBuilder skipShort() {
        this.skip(stringBuilder, Bytes.SIZEOF_SHORT);
        return this;
    }

    public RegexpBuilder skipInt() {
        this.skip(stringBuilder, Bytes.SIZEOF_INT);
        return this;
    }

    public RegexpBuilder skipLong() {
        this.skip(stringBuilder, Bytes.SIZEOF_LONG);
        return this;
    }

    public RegexpBuilder appendMultiValue(long[] array) {
        List<byte[]> list = new ArrayList<byte[]>();
        for (long number : array) {
            list.add(Bytes.toBytes(number));
        }
        this.addMultiBytes(stringBuilder, list);
        return this;
    }

    public RegexpBuilder appendMultiValue(int[] array) {
        List<byte[]> list = new ArrayList<byte[]>();
        for (int number : array) {
            list.add(Bytes.toBytes(number));
        }
        this.addMultiBytes(stringBuilder, list);
        return this;
    }

    public RegexpBuilder appendMultiValue(Integer[] array) {
        List<byte[]> list = new ArrayList<byte[]>();
        for (int number : array) {
            list.add(Bytes.toBytes(number));
        }
        this.addMultiBytes(stringBuilder, list);
        return this;
    }

    public RegexpBuilder appendMultiValue(Collection<Integer> array) {
        List<byte[]> list = new ArrayList<byte[]>();
        for (int number : array) {
            list.add(Bytes.toBytes(number));
        }
        this.addMultiBytes(stringBuilder, list);
        return this;
    }

    public RegexpBuilder appendMultiValue(String[] array) {
        List<byte[]> list = new ArrayList<byte[]>();
        for (String str : array) {
            list.add(Bytes.toBytes(str));
        }
        this.addMultiBytes(stringBuilder, list);
        return this;
    }

    public String buildRegexp() {
        return stringBuilder.toString();
    }

    private void addMultiBytes(final StringBuilder buf, List<byte[]> bytes) {
        buf.append("(?:");
        for (int i = 0; i < bytes.size(); i++) {
            if (i > 0) {
                buf.append('|');
            }
            addBytes(buf, bytes.get(i));
        }
        buf.append(')');
    }

    /**
     * Skip length in regexp expression
     */
    private void skip(final StringBuilder buf, int skipLength) {
        if (skipLength > 0) {
            if (buf.length() <= 4) {
                // skip from the beginning
                buf.append("^.{").append(skipLength).append("}*");
            } else {
                // skip in middle
                buf.append("(.{").append(skipLength).append("})*");
            }
        }
    }

    /**
     * 不带星号
     */
    public void skipWithNoAsterisk(int skipLength) {
        if (skipLength > 0) {
            if (stringBuilder.length() <= 4) {
                // skip from the beginning
                stringBuilder.append("^.{").append(skipLength).append("}");
            } else {
                // skip in middle
                stringBuilder.append("(.{").append(skipLength).append("})");
            }
        }
    }

    /**
     * Appends the given bytes data to the given buffer, followed by "\\E".
     */
    private void addBytes(final StringBuilder buf, final byte[] bytes) {
        buf.append("\\Q");
        boolean backslash = false;
        for (final byte b : bytes) {

            buf.append((char)(b & 0xFF));
            /*
             * If we saw a `\' and now we have a `E'. So we just terminated the
             * quoted section because we just added \E to `buf'. So let's put a
             * literal \E now and start quoting again.
             */
            if (b == 'E' && backslash) {
                buf.append("\\\\E\\Q");
            } else {
                backslash = b == '\\';
            }
        }
        buf.append("\\E");
    }

}
