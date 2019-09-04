package io.etrace.common.queue;

public interface QueueCodec {
    byte[] encode(Object data);

    Object decode(byte[] data);
}
