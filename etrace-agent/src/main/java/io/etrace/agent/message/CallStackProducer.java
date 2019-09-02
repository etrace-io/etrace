package io.etrace.agent.message;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.TimeoutException;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.event.MessageEvent;
import io.etrace.agent.message.io.MessageSender;
import io.etrace.agent.stat.MessageStats;
import io.etrace.common.modal.CallStack;
import io.etrace.common.modal.JSONCodec;
import io.etrace.common.modal.Message;
import io.etrace.common.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CallStackProducer {
    public static final String EMPTY = "-1";
    @Inject
    private MessageSender messageSender;
    @Inject
    private MessageStats stats;
    private MessageProducer messageProducer;
    private ProducerContext<MessageEvent> context;
    private Timer timer;

    public CallStackProducer() {
        context = new ProducerContext();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;
        messageProducer = new MessageProducer();

        // The factory for the event
        MessageEvent.MessageEventFactory factory = new MessageEvent.MessageEventFactory();

        context.build("CallStack-Producer", bufferSize, new EventConsumer(), factory);

        timer = new Timer("CallStackProducer-Timer-" + System.currentTimeMillis());
        int pullIntervalSeconds = 2;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (context.getRingBuffer() != null) {
                    context.getRingBuffer().tryPublishEvent(messageProducer, EMPTY, EMPTY, null);//heartbeat
                }
            }
        }, 0, pullIntervalSeconds * 1000);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
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
            context.getRingBuffer().tryPublishEvent(messageProducer, EMPTY, EMPTY, null);//heartbeat
            context.getDisruptor().shutdown(2, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {
        }
        timer.cancel();
        messageSender.shutdown();
    }

    class MessageProducer implements EventTranslatorThreeArg<MessageEvent, String, String, Message> {
        @Override
        public void translateTo(MessageEvent event, long sequence, String requestId, String messageId,
                                Message message) {
            event.reset(AgentConfiguration.getServiceName(),
                context.getHostIp(), context.getHostName(), requestId, messageId, message,
                context.getCluster(), context.getEzone(), context.getIdc(), context.getMesosTaskId(),
                context.getEleapposLabel(), context.getEleapposSlaveFqdn(), context.getInstance());
        }
    }

    class EventConsumer implements EventHandler<MessageEvent> {
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
                generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
                generator.writeStartArray();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
            if (generator != null) {
                try {
                    CallStack callStack = event.getCallStack();
                    if (callStack != null
                        && !EMPTY.equals(callStack.getRequestId())
                        && !EMPTY.equals(callStack.getId())
                        && callStack.getMessage() != null) {
                        totalCount++;
                        JSONCodec.encodeAsArray(callStack, generator);
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
                        generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
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
                    if (totalCount > 0 && AgentConfiguration.isEnableTrace()) {
                        messageSender.send(baos.toByteArray(), totalCount, null);
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
