package io.etrace.plugins.prometheus.pushgateway.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 */
public class CollectorSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorSocket.class);


    private final static long LONG_TIME_CLOSE = 330 * 1000;
    private long lastVisitTime = System.currentTimeMillis();

    private CollectorConnection connection;


    public boolean send(byte[] head, byte[] chunk) {
        return sendTryTwice(head, chunk);
    }


    public void shutdown() {
        closeConnection();
    }

    private void getConnection() {
        if (connection == null || !connection.isOpen()) {
            closeConnection();
            CollectorConnection newConnect = new CollectorConnection();
            newConnect.openConnection();
            this.connection = newConnect;
//        } else {
//            closeConnection();
//            CollectorConnection newConnect = new CollectorConnection();
//            newConnect.openConnection();
//            this.connection = newConnect;
        }

        if (!connection.isOpen()) {
            connection.openConnection();
        }
    }


    private boolean openConnection() {
        getConnection();
        return isOpen();
    }

    private SocketChannel getClient() {
        openConnection();
        if (null == connection) {
            return null;
        }
        return connection.getSocketClient();
    }


    private boolean isOpen() {
        return connection != null && connection.isOpen();
    }

    private void closeConnection() {
        if (connection != null) {
            connection.closeConnection();
        }
    }


    public void tryCloseConnWhenLongTime() {
        if (System.currentTimeMillis() - lastVisitTime > LONG_TIME_CLOSE && connection != null && connection.isOpen()) {
            connection.closeConnection();
            lastVisitTime = System.currentTimeMillis();
        }
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

    private boolean send0(byte[] head, byte[] chunk) {
        SocketChannel client = getClient();
        if (null == client) {
            return false;
        }
        return tcpSend(client, head, chunk);

    }


    private boolean tcpSend(SocketChannel socketChannel, byte[] head, byte[] chunk) {
        if (null == socketChannel) {
            return false;
        }
        try {
            ByteBuffer buffer = ByteBuffer.allocate(12 + head.length + chunk.length);
            buffer.putInt(8 + head.length + chunk.length);
            buffer.putInt(head.length);
            buffer.put(head);
            buffer.putInt(chunk.length);
            buffer.put(chunk);
            buffer.flip();
            while (buffer.hasRemaining()) {
                if (socketChannel.write(buffer) == 0){
                    break;
                }
            }
            //compress data
            buffer.compact();
            return buffer.position() == 0;
        } catch (Throwable e) {
            LOGGER.error("tcp send error", e);
            return false;
        }
    }
}
