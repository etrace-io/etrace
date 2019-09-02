package io.etrace.agent.message.network;

import io.etrace.agent.config.CollectorRegistry;
import io.etrace.common.modal.Collector;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TcpConnection extends AbstractConnection {
    private Selector selector;
    private SocketChannel socketChannel;
    private Collector currentCollector;

    public TcpConnection(int senderTimeout) {
        super(senderTimeout);
    }

    @Override
    public void openConnection() {
        int collectorSize = CollectorRegistry.getInstance().getTcpCollectorSize();
        if (collectorSize < 1) {
            return;
        }
        for (int i = 0; i < collectorSize; i++) {   //if all collector is open error, return
            try {
                currentCollector = CollectorRegistry.getInstance().getTcpCollector();

                if (currentCollector == null) {
                    return;
                }
                selector = Selector.open();
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(true);
                socketChannel.socket().setTcpNoDelay(true);
                socketChannel.socket().setKeepAlive(true);
                socketChannel.socket().setSoTimeout(timeout);

                socketChannel.socket().connect(
                    new InetSocketAddress(currentCollector.getIp(), currentCollector.getPort()), timeout);
                return;
            } catch (Throwable ignore) {
                closeConnection();
            }
        }
    }

    @Override
    public boolean isOpen() {
        return null != selector && selector.isOpen() && null != socketChannel && socketChannel.isConnected()
            && socketChannel.isOpen();
    }

    @Override
    public void closeConnection() {
        try {
            if (null != socketChannel) {
                socketChannel.socket().close();
                socketChannel.close();
                socketChannel = null;
            }

            if (null != selector) {
                selector.close();
                if (null != selector) {
                    selector.wakeup();
                    selector = null;
                }
            }
        } catch (Exception e) {
            //ignore
            socketChannel = null;
            selector = null;
        }
        currentCollector = null;
    }

    @Override
    public Object getSocketClient() {
        return socketChannel;
    }

}
