package io.etrace.collector.netty.thrift;

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
