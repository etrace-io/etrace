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

package io.etrace.agent.io;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.etrace.agent.Version;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.config.CollectorRegistry;
import io.etrace.agent.message.event.DataEvent;
import io.etrace.agent.stat.TCPStats;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.etrace.common.util.ThreadUtil;

import java.util.concurrent.TimeUnit;

public class TcpMessageSender implements MessageSender {
    public static final String TRACE = "Trace";
    public static final String Metric = "Metric";

    public static final String METRIC_TCP_MESSAGE_SENDER = "metricTCPMessageSender";

    private volatile boolean active;
    private long startTime = System.currentTimeMillis();
    private TCPStats stats;
    private MessageHeader messageHeader;
    private Disruptor<DataEvent> disruptor;
    private RingBuffer<DataEvent> ringBuffer;
    private DataProducer messageProducer;

    public TcpMessageSender(String messageType, TCPStats stats) {
        active = true;
        this.stats = stats;
        messageHeader = new MessageHeader();
        messageHeader.setHostIp(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress());
        messageHeader.setHostName(NetworkInterfaceHelper.INSTANCE.getLocalHostName());
        messageHeader.setMessageType(messageType);
        messageHeader.setVersion(Version.TraceVersion);
        // The factory for the event
        DataEvent.DataEventFactory factory = new DataEvent.DataEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 256;

        messageProducer = new DataProducer();

        // Construct the Disruptor
        disruptor = new Disruptor<>(factory, bufferSize, r -> {
            Thread t = new Thread(r);
            t.setName(messageType + "-TCPSender");
            t.setDaemon(true);
            return t;
        }, ProducerType.SINGLE, new LiteBlockingWaitStrategy());
        disruptor.handleEventsWith(new DataConsumer());
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<DataEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, DataEvent event) {

            }

            @Override
            public void handleOnStartException(Throwable ex) {

            }

            @Override
            public void handleOnShutdownException(Throwable ex) {

            }
        });
        ringBuffer = disruptor.getRingBuffer();
        disruptor.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    @Override
    public void shutdown() {
        if (System.currentTimeMillis() - startTime < 2000) {
            ThreadUtil.sleepForSecond(2);
        }
        active = false;
        try {
            disruptor.shutdown(2, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {
        }
    }

    @Override
    public int getQueueSize() {
        return ringBuffer.getBufferSize() - (int)ringBuffer.remainingCapacity();
    }

    @Override
    public void send(byte[] chunk, int messageCount) {
        stats.incTotalSize(chunk.length);
        if (!ringBuffer.tryPublishEvent(messageProducer, chunk, messageCount)) {
            stats.incTcpLoss(messageCount);
            stats.incLossSize(chunk.length);
            ThreadUtil.sleep(0);
        }
    }
    // Mockito

    class DataProducer implements EventTranslatorTwoArg<DataEvent, byte[], Integer> {

        @Override
        public void translateTo(DataEvent event, long sequence, byte[] data, Integer totalCount) {
            event.reset(data, totalCount);
        }
    }

    class DataConsumer implements EventHandler<DataEvent> {
        private final static int MAX_SLEEP_TIME = 60000;
        private final static int INVALID_TIME = 30000;
        private Client socketClient;

        DataConsumer() {
            socketClient = SocketClientFactory.getClient();
        }

        @Override
        public void onEvent(DataEvent event, long sequence, boolean endOfBatch) throws Exception {
            byte[] chunk = event.getBuffer();
            int count = event.getCount();
            if (chunk != null && count > 0) {
                stats.incTcpPollCount(count);
                if (openThriftConnection(sequence)) {
                    internalSend(chunk, count);
                } else {
                    stats.incLossSize(chunk.length);
                    stats.incLossInNet(count);
                }
            }
            if (endOfBatch) {
                socketClient.tryCloseConnWhenLongTime();
            }
            event.clear();
        }

        private boolean openThriftConnection(long sequence) {
            int sleepTime = 10;
            Long start = null;
            long nextSequence = sequence;
            while (!socketClient.openConnection()) {
                if (!active) {
                    return false;
                }
                if (start == null) {
                    start = System.currentTimeMillis();
                }
                long sleptTime = System.currentTimeMillis() - start;
                if (sleptTime >= INVALID_TIME) {
                    CollectorRegistry.getInstance().setIsAvailable(false);
                }
                if (sleptTime >= INVALID_TIME && nextSequence < (sequence + getQueueSize() - 1)) {
                    nextSequence = clearEvent(nextSequence, sequence);
                }
                if (sleepTime > MAX_SLEEP_TIME) {
                    sleepTime = MAX_SLEEP_TIME;
                } else if (sleepTime < MAX_SLEEP_TIME) {
                    sleepTime = sleepTime * 2;
                }
                ThreadUtil.sleep(sleepTime);
            }
            if (start != null) {
                CollectorRegistry.getInstance().setIsAvailable(true);
            }
            return true;
        }

        private long clearEvent(long nextSequence, long sequence) {
            while (nextSequence < (sequence + getQueueSize() - 1)) {
                nextSequence++;
                DataEvent dataEvent = ringBuffer.get(nextSequence);
                if (dataEvent == null) {
                    return nextSequence;
                }
                byte[] chunk = dataEvent.getBuffer();
                int count = dataEvent.getCount();
                if (chunk != null && count > 0) {
                    dataEvent.clear();
                    stats.incLossSize(chunk.length);
                    stats.incLossInNet(count);
                } else {
                    return nextSequence;
                }
            }
            return nextSequence;
        }

        private void internalSend(byte[] data, int count) {
            int len = data.length;
            try {
                messageHeader.setAppId(AgentConfiguration.getAppId());
                messageHeader.setAst(System.currentTimeMillis());
                messageHeader.setInstance(AgentConfiguration.getInstance());
                boolean success = socketClient.send(JSONUtil.toBytes(messageHeader), data);
                if (success) {
                    stats.incSuccessCount(count);
                } else {
                    stats.incLossSize(len);
                    stats.incLossInNet(count);
                }
            } catch (Exception e) {
                stats.incLossSize(len);
                stats.incLossInNet(count);
            }
        }
    }

}
