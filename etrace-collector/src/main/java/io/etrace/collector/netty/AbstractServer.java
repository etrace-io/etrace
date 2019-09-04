package io.etrace.collector.netty;

public abstract class AbstractServer implements Server {

    public abstract void shutdownServer();

    @Override
    public void shutdown() {
        shutdownServer();
    }

}
