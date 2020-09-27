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

public abstract class AbstractSocketClient implements SocketClient {

    private final static long LONG_TIME_CLOSE = 330 * 1000;
    protected Connection connection;
    protected boolean useTcp;
    private long lastVisitTime = System.currentTimeMillis();
    private int SENDER_TIMEOUT;

    public AbstractSocketClient(int senderTimeout) {
        this.SENDER_TIMEOUT = senderTimeout;
    }

    protected void getConnection() {
        useTcp = CollectorRegistry.getInstance().isUseTcp();
        if (connection == null || !connection.isOpen()) {
            closeConnection();
            Connection newConnect;
            if (useTcp) {
                newConnect = new TcpConnection(SENDER_TIMEOUT);
            } else {
                newConnect = new ThriftConnection(SENDER_TIMEOUT);
            }
            newConnect.openConnection();
            this.connection = newConnect;
        } else {
            if (useTcp) {
                //tcp
                if (connection instanceof ThriftConnection) {
                    closeConnection();
                    Connection newConnect = new TcpConnection(SENDER_TIMEOUT);
                    newConnect.openConnection();
                    this.connection = newConnect;
                }
            } else {
                //thrift
                if (connection instanceof TcpConnection) {
                    closeConnection();
                    Connection newConnect = new ThriftConnection(SENDER_TIMEOUT);
                    newConnect.openConnection();
                    this.connection = newConnect;
                }
            }
            if (!connection.isOpen()) {
                connection.openConnection();
            }
        }
    }

    @Override
    public boolean send(byte[] head, byte[] chunk) {
        return sendTryTwice(head, chunk);
    }

    private boolean sendTryTwice(byte[] head, byte[] chunk) {
        //when send error will try send again
        for (int action = 0; action < 2; action++) {
            if (send0(head, chunk)) {
                return true;
            }
        }
        return false;
    }

    public abstract boolean send0(byte[] head, byte[] chunk);

    @Override
    public void tryCloseConnWhenLongTime() {
        if (System.currentTimeMillis() - lastVisitTime > LONG_TIME_CLOSE && connection != null && connection.isOpen()) {
            connection.closeConnection();
            lastVisitTime = System.currentTimeMillis();
        }
    }

    protected boolean isOpen() {
        return connection != null && connection.isOpen();
    }

    public void closeConnection() {
        if (connection != null) {
            connection.closeConnection();
        }
    }

    @Override
    public void shutdown() {
        closeConnection();
    }
}
