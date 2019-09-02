package io.etrace.agent.message.network;

import io.etrace.agent.config.CollectorRegistry;
import io.etrace.agent.stat.TCPStats;

public abstract class AbstractSocketClient implements SocketClient {

    private final static long LONG_TIME_CLOSE = 330 * 1000;
    protected Connection connection;
    protected String name;
    protected boolean useTcp;
    private long lastVisitTime = System.currentTimeMillis();
    private int SENDER_TIMEOUT;
    private Timer timer;
    private TCPStats tcpStats;

    public AbstractSocketClient(int senderTimeout) {
        this.name = Thread.currentThread().getName();
        this.SENDER_TIMEOUT = senderTimeout;
        timer = new Timer(senderTimeout);
        timer.setDaemon(true);
        timer.setName(this.name + "-Timer");
        timer.start();
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
        for (int action = 0; action < 2; action++) {    //when send error will try send again
            if (send0(head, chunk)) {
                return true;
            }
        }
        return false;
    }

    private boolean send0(byte[] head, byte[] chunk) {
        timer.checkStart();
        try {
            return send1(head, chunk);
        } finally {
            timer.checkStop();
        }
    }

    public abstract boolean send1(byte[] head, byte[] chunk);

    @Override
    public void tryCloseConnWhenLongTime() {
        if (System.currentTimeMillis() - lastVisitTime > LONG_TIME_CLOSE && connection != null && connection.isOpen()) {
            connection.closeConnection();
            lastVisitTime = System.currentTimeMillis();
        }
    }

    public boolean isOpen() {
        return connection != null && connection.isOpen();
    }

    public void closeConnection() {
        if (connection == null) {
            return;
        }
        connection.closeConnection();
    }

    private void closeConnectionWhenTimeout() {
        try {
            closeConnection();
        } finally {
            if (null != tcpStats) {
                tcpStats.incTimeoutCount(1);
            }
        }
    }

    @Override
    public void shutdown() {
        if (null != timer) {
            timer.is_running = false;
            closeConnection();
            timer.checkStop();
        }
    }

    public void setTcpStats(TCPStats tcpStats) {
        this.tcpStats = tcpStats;
    }

    private class Timer extends Thread {
        /**
         * Rate at which timer is checked
         */
        private int m_rate = 200;

        /**
         * Length of timeout
         */
        private int m_length;

        /**
         * Time elapsed
         */
        private int m_elapsed;

        private boolean m_timeReset;

        private boolean is_running = true;

        /**
         * Creates a timer of a specified length
         *
         * @param length Length of time before timeout occurs
         */
        Timer(int length) {
            // Assign to member variable
            m_length = length;

            // Set time elapsed
            m_elapsed = 0;
            //
            m_timeReset = false;
        }

        /**
         * Resets the timer back to zero
         */
        void checkStart() {
            m_elapsed = 0;

            m_timeReset = true;
        }

        void checkStop() {
            m_elapsed = 0;
            m_timeReset = false;
        }

        /**
         * Performs timer specific code
         */
        @Override
        public void run() {
            // Keep looping
            while (is_running) {
                try {
                    // Put the timer to checkStop
                    try {
                        Thread.sleep(m_rate);
                    } catch (InterruptedException ioe) {
                        continue;
                    }
                    if (m_timeReset) {
                        // Use 'synchronized' to prevent conflicts
                        synchronized (this) {
                            // Increment time remaining
                            m_elapsed += m_rate;
                            // Check to see if the time has been exceeded
                            if (m_elapsed > m_length) {
                                // Trigger a timeout
                                closeConnectionWhenTimeout();
                            }
                        }
                    }
                } catch (Throwable e) {
                    checkStop();
                }
            }
        }
    }
}
