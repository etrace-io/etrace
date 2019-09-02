package io.etrace.agent.message.network;

import io.etrace.agent.config.CollectorRegistry;
import io.etrace.common.modal.Collector;
import io.etrace.common.rpc.MessageService;
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

}
