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

package io.etrace.agent.message.callstack;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.TimeoutException;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.io.MessageSender;
import io.etrace.agent.message.QueueContext;
import io.etrace.agent.stat.CallstackStats;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.Message;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CallstackQueue {
    public static final long PULL_INTERVAL_IN_MILLISECOND = TimeUnit.SECONDS.toMillis(2);
    private static final String EMPTY = "-1";
    @Inject
    private MessageSender messageSender;
    @Inject
    private CallstackStats stats;
    private MessageProducer messageProducer;
    private QueueContext<CallstackEvent> context;
    private ScheduledExecutorService executorService;

    public CallstackQueue() {
        context = new QueueContext<>();

        messageProducer = new MessageProducer();

        // The factory for the event
        CallstackEvent.MessageEventFactory factory = new CallstackEvent.MessageEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;
        context.build("CallStack-Producer", bufferSize, new EventConsumer(), factory);

        executorService = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("CallstackQueue-Timer-%d").build());

        executorService.scheduleAtFixedRate(() -> {
            if (context.getRingBuffer() != null) {
                context.getRingBuffer().tryPublishEvent(messageProducer, EMPTY, EMPTY, null);//heartbeat
            }
        }, 0, PULL_INTERVAL_IN_MILLISECOND, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void produce(String requestId, String rpcId, Message message) {
        if (!context.isActive()) {
            return;
        }
        stats.incTotalCount();
        if (!context.getRingBuffer().tryPublishEvent(messageProducer, requestId, rpcId, message)) {
            stats.incLoss();
            ThreadUtil.sleep(0);
        }
    }

    public int getQueueSize() {
        return context.getQueueSize();
    }

    public void shutdown() {
        try {
            context.setActive(false);

            //for heartbeat ??
            context.getRingBuffer().tryPublishEvent(messageProducer, EMPTY, EMPTY, null);
            context.getDisruptor().shutdown(2, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {
        }
        executorService.shutdown();
        messageSender.shutdown();
    }

    class MessageProducer implements EventTranslatorThreeArg<CallstackEvent, String, String, Message> {
        @Override
        public void translateTo(CallstackEvent event, long sequence, String requestId, String messageId,
                                Message message) {
            event.reset(AgentConfiguration.getAppId(),
                context.getHostIp(), context.getHostName(), requestId, messageId, message,
                AgentConfiguration.getExtraProperties());
        }
    }

    class EventConsumer implements EventHandler<CallstackEvent> {
        private JsonFactory jsonFactory;
        private ByteArrayOutputStream baos;
        private JsonGenerator generator;
        private int totalCount;
        private int maxSize = 1024 * 1024 * 2;
        private int maxCount = 300;
        private long start = System.currentTimeMillis();

        public EventConsumer() {
            jsonFactory = new JsonFactory();
            baos = new ByteArrayOutputStream();
            try {
                generator = jsonFactory.createGenerator(baos, JsonEncoding.UTF8);
                generator.writeStartArray();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void onEvent(CallstackEvent event, long sequence, boolean endOfBatch) throws Exception {
            if (generator != null) {
                try {
                    CallStackV1 callStack = event.getCallStack();
                    if (callStack != null
                        && !EMPTY.equals(callStack.getRequestId())
                        && !EMPTY.equals(callStack.getId())
                        && callStack.getMessage() != null) {
                        totalCount++;
                        JSONCodecV1.encodeCallstackByArrayFormat(callStack, generator);
                        callStack.clear();
                    }
                } catch (Exception e) {
                    stats.incLoss(totalCount);
                    if (generator != null) {
                        generator.flush();
                        generator.close();
                    }
                    if (baos != null) {
                        totalCount = 0;
                        start = System.currentTimeMillis();
                        baos.reset();
                    }
                    try {
                        generator = jsonFactory.createGenerator(baos, JsonEncoding.UTF8);
                        generator.writeStartArray();//start new array
                    } catch (IOException ignore) {
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            if (totalCount >= maxCount || baos.size() >= maxSize || endTime - start >= 2000 || !context.isActive()) {
                flush();
            }
            event.clear();
        }

        private void flush() throws IOException {
            if (generator != null) {
                generator.writeEndArray();
                generator.flush();
                generator.writeStartArray();//start new array
            }
            if (baos != null && baos.size() > 0) {
                try {
                    /**
                     * 关闭后不再向collector发送trace数据
                     */
                    if (totalCount > 0) {
                        messageSender.send(baos.toByteArray(), totalCount);
                    }
                } finally {
                    totalCount = 0;
                    start = System.currentTimeMillis();
                    baos.reset();
                }
            }
        }
    }
}
