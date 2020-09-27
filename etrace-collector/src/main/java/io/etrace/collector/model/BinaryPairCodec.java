package io.etrace.collector.model;

import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.queue.QueueCodec;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BinaryPairCodec implements QueueCodec {
    private final static Logger LOGGER = LoggerFactory.getLogger(BinaryPairCodec.class);

    @Override
    public byte[] encode(Object data) {
        if (!(data instanceof Pair)) {
            return null;
        }
        Pair<MessageHeader, byte[]> pair = (Pair<MessageHeader, byte[]>)data;
        try {
            byte[] header = JSONUtil.toBytes(pair.getKey());
            byte[] message = pair.getValue();
            ByteBuffer buffer = ByteBuffer.allocate(header.length + message.length + 8);
            buffer.putInt(header.length);
            buffer.put(header);
            buffer.putInt(message.length);
            buffer.put(message);
            return buffer.array();
        } catch (IOException e) {
            LOGGER.error("encode error:", e);
        }
        return null;
    }

    @Override
    public Object decode(byte[] data) {
        try {
            Pair<MessageHeader, byte[]> pair = new Pair<>();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int headerLen = buffer.getInt();
            byte[] header = new byte[headerLen];
            buffer.get(header);
            int messageLen = buffer.getInt();
            byte[] message = new byte[messageLen];
            buffer.get(message);
            pair.setKey(JSONUtil.toObject(header, MessageHeader.class));
            pair.setValue(message);
            return pair;
        } catch (IOException e) {
            LOGGER.error("decode error:", e);
        }
        return null;
    }
}
