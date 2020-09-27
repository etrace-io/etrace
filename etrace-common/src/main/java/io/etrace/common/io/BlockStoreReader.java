/*
 * Copyright 2020 etrace.io
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


package io.etrace.common.io;

import io.etrace.common.message.metric.util.CodecUtil;
import org.xerial.snappy.SnappyInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BlockStoreReader {

    public static Iterable<byte[]> newSnappyIterator(byte[] data) {
        return () -> new SnappyBlockIterator(data);
    }

    private static abstract class AbstractSnappyBlockIterator<E> implements Iterator<E> {
        protected DataInputStream in;
        protected boolean canRead = true;

        AbstractSnappyBlockIterator(byte[] data) {
            try {
                in = new DataInputStream(new SnappyInputStream(new ByteArrayInputStream(data)));
            } catch (IOException e) {
                canRead = false;
            }
        }

        AbstractSnappyBlockIterator(InputStream inputStream) {
            try {
                if (inputStream instanceof DataInputStream) {
                    in = (DataInputStream)inputStream;
                } else if (inputStream instanceof SnappyInputStream) {
                    in = new DataInputStream(inputStream);
                } else {
                    in = new DataInputStream(new SnappyInputStream(inputStream));
                }
            } catch (IOException e) {
                canRead = false;
            }
        }

        @Override
        public boolean hasNext() {
            if (!canRead) {
                return false;
            }
            try {
                return in.available() > 0;
            } catch (IOException e) {
                return false;
            }
        }
    }

    protected static class SnappyBlockIterator extends AbstractSnappyBlockIterator<byte[]> {

        SnappyBlockIterator(byte[] data) {
            super(data);
        }

        SnappyBlockIterator(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public byte[] next() {
            byte[] currentData;
            try {
                currentData = CodecUtil.readLengthPrefixData(in);
            } catch (IOException e) {
                canRead = false;
                return null;
            }
            return currentData;
        }
    }

}
