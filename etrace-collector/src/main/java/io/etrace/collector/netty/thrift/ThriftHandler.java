package io.etrace.collector.netty.thrift;

import io.etrace.collector.netty.DefaultHandler;
import io.etrace.common.rpc.MessageService;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

public class ThriftHandler implements MessageService.Iface {
    private DefaultHandler handler;
    private static final String serverType = "thrift";

    public ThriftHandler(DefaultHandler handler) {
        this.handler = handler;
    }

    @Override
    public void send(ByteBuffer head, ByteBuffer message) throws TException {
        handler.process(head.array(), message.array(), serverType);
    }
}
