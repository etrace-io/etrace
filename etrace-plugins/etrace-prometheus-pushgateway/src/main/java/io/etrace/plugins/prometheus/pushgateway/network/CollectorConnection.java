package io.etrace.plugins.prometheus.pushgateway.network;

import io.etrace.common.message.agentconfig.Collector;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;

public class CollectorConnection {

    protected int timeout = 5 * 1000;
    private Selector selector;
    private SocketChannel socketChannel;
    private Collector currentCollector;

    public CollectorConnection() {
    }

    public void openConnection() {
        List<Collector> collectorList = CollectorTcpAddressRegistry.getInstance().getCollectorList();
        int collectorSize = collectorList.size();
        if (collectorSize < 1) {
            return;
        }
        //if all collector is open error, return
        for (int i = 0; i < collectorSize; i++) {
            try {
                currentCollector = CollectorTcpAddressRegistry.getTcpCollector();
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

    public boolean isOpen() {
        return null != selector && selector.isOpen() && null != socketChannel && socketChannel.isConnected()
            && socketChannel.isOpen();
    }

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

    public SocketChannel getSocketClient() {
        return socketChannel;
    }

}

