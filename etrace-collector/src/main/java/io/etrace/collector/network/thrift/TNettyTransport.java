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

package io.etrace.collector.network.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Wraps incoming channel buffer into TTransport and provides a output buffer.
 */
public class TNettyTransport extends TTransport {
    private final Channel channel;
    private final ThriftMessage in;

    public TNettyTransport(Channel channel, ByteBuf in) {
        this(channel, new ThriftMessage(in, ThriftTransportType.UNKNOWN));
    }

    public TNettyTransport(Channel channel, ThriftMessage in) {
        this.channel = channel;
        this.in = in;

        in.getBuffer().retain();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void open() throws TTransportException {
        // no-op
    }

    @Override
    public void close() {
        // no-op
        channel.close();
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws TTransportException {
        int _read = Math.min(in.getBuffer().readableBytes(), length);
        in.getBuffer().readBytes(bytes, offset, _read);
        return _read;
    }

    @Override
    public int readAll(byte[] bytes, int offset, int length) throws TTransportException {
        in.getBuffer().readBytes(bytes, offset, length);
        return length;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws TTransportException {
        //nothing to do
    }

    @Override
    public void flush() throws TTransportException {
        //nothing to do
    }

    public void release() {
        in.getBuffer().release();
    }
}
