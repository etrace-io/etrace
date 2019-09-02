package io.etrace.agent.message.network;

public abstract class AbstractConnection implements Connection {

    protected int timeout;

    public AbstractConnection(int timeout) {
        this.timeout = timeout;
    }
}
