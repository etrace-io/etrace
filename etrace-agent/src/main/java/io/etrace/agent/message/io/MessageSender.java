package io.etrace.agent.message.io;

public interface MessageSender {
    void shutdown();

    int getQueueSize();

    void send(byte[] chunk, int count, String key);
}
