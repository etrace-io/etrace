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
import io.etrace.common.thrift.MessageService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

public class ThriftConnection extends AbstractConnection {

    private TFramedTransport transport;
    private TSocket socket;
    private TBinaryProtocol protocol;
    private MessageService.Client collector;
    private Collector currentCollector;

    public ThriftConnection(int senderTimeout) {
        super(senderTimeout);
    }

    @Override
    public void openConnection() {
        int collectorSize = CollectorRegistry.getInstance().getCollectorSize();
        if (collectorSize < 1) {
            System.out.println("Etrace-Agent: no Available Collectors, Can't open Thrift Connection. Check "
                + "ConfigManager for collector address api.");
            return;
        }
        for (int i = 0; i < collectorSize; i++) {   //if all collector is open error, return
            try {
                currentCollector = CollectorRegistry.getInstance().getThriftCollector();
                if (currentCollector == null) {
                    return;
                }
                socket = new TSocket(currentCollector.getIp(), currentCollector.getPort());
                socket.setTimeout(timeout);
                transport = new TFramedTransport(socket);
                protocol = new TBinaryProtocol(transport);
                collector = new MessageService.Client(protocol);
                transport.open();
                return;
            } catch (Throwable ignore) {
                closeConnection();
            }
        }
    }

    @Override
    public boolean isOpen() {
        return transport != null && transport.isOpen() && socket != null && socket.isOpen() && protocol != null
            && collector != null;
    }

    @Override
    public void closeConnection() {
        if (transport != null && transport.isOpen()) {
            try {
                transport.close();
            } catch (Exception ignored) {
            }
            transport = null;
        }
        if (socket != null && socket.isOpen()) {
            try {
                socket.close();
            } catch (Exception ignore) {

            }
            socket = null;
        }
        if (protocol != null) {
            protocol = null;
        }
        if (collector != null) {
            collector = null;
        }
        currentCollector = null;
    }

    @Override
    public Object getSocketClient() {
        return collector;
    }

    @Override
    public Collector getCollector() {
        return currentCollector;
    }
}
