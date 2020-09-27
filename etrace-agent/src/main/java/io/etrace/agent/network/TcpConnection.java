/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.agent.network;

import io.etrace.agent.config.CollectorRegistry;
import io.etrace.common.message.agentconfig.Collector;

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
