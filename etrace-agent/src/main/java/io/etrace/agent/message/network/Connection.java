package io.etrace.agent.message.network;

public interface Connection {

    void openConnection();

    boolean isOpen();

    void closeConnection();

    Object getSocketClient();

}
