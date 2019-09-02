package io.etrace.agent.message.io;

import io.etrace.agent.config.CollectorRegistry;
import io.etrace.agent.message.network.AbstractSocketClient;
import io.etrace.common.rpc.MessageService;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client extends AbstractSocketClient {

    public Client() {
        this(6 * 1000);
    }

    public Client(int senderTimeout) {
        super(senderTimeout);
        useTcp = CollectorRegistry.getInstance().isUseTcp();
    }

    private boolean thriftSend(MessageService.Client client, byte[] head, byte[] chunk) {
        try {
            client.send(ByteBuffer.wrap(head), ByteBuffer.wrap(chunk));
            return true;
        } catch (TException e) {
            closeConnection();
            return false;
        } finally {
            if (!CollectorRegistry.getInstance().isLongConnection()) {
                closeConnection();
            }
        }
    }

    private boolean tcpSend(SocketChannel socketChannel, byte[] head, byte[] chunk) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(12 + head.length + chunk.length);
            buffer.putInt(8 + head.length + chunk.length);
            buffer.putInt(head.length);
            buffer.put(head);
            buffer.putInt(chunk.length);
            buffer.put(chunk);
            buffer.flip();

            while (buffer.hasRemaining()) {
                if (socketChannel.write(buffer) == 0) { break; }
            }
            //compress data
            buffer.compact();
            return buffer.position() == 0;
        } catch (Throwable e) {
            closeConnection();
            return false;
        } finally {
            if (!CollectorRegistry.getInstance().isLongConnection()) {
                closeConnection();
            }
        }
    }

    @Override
    public boolean send1(byte[] head, byte[] chunk) {
        Object client = getClient();
        if (null == client) {
            return false;
        }
        if (client instanceof SocketChannel) {
            return tcpSend((SocketChannel)client, head, chunk);
        } else {
            return thriftSend((MessageService.Client)client, head, chunk);
        }
    }

    @Override
    public boolean openConnection() {
        getConnection();
        return isOpen();
    }

    private Object getClient() {
        openConnection();
        if (null == connection) {
            return null;
        }
        return connection.getSocketClient();
    }
}
