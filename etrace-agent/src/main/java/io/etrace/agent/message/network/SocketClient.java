package io.etrace.agent.message.network;

public interface SocketClient {

    boolean send(byte[] head, byte[] chunk);

    void tryCloseConnWhenLongTime();

    boolean openConnection();

    void shutdown();

}
