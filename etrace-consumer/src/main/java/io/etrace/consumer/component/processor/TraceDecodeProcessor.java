package io.etrace.consumer.component.processor;

import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.MessageItem;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Processor;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.etrace.consumer.metrics.MetricsService;
import io.etrace.consumer.model.MessageBlock;
import io.etrace.consumer.util.CallStackUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.xerial.snappy.SnappyInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.etrace.consumer.metrics.MetricName.*;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TraceDecodeProcessor extends DefaultAsyncTask implements Processor {
    public final Logger LOGGER = LoggerFactory.getLogger(TraceDecodeProcessor.class);

    @Autowired
    public MetricsService metricsService;

    public Timer hdfsTimer;
    public Counter parseError;

    public TraceDecodeProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
    }

    @Override
    public void startup() {
        super.startup();
        hdfsTimer = Metrics.timer(TASK_DURATION, Tags.of("type", "decode-to-hdfs"));
        parseError = Metrics.counter(CALLSTACK_PARSE_ERROR, Tags.empty());
    }

    @Override
    public void processEvent(Object key, Object event) {
        if (null == event || !(event instanceof MessageAndMetadata)) {
            return;
        }

        byte[] body = ((MessageAndMetadata<byte[], byte[]>)event).message();

        try {
            List<MessageItem> items = decode(body);
            writeToHDFS(items, body);
        } catch (Exception e) {
            error(body, e);
        }
    }

    public void error(byte[] body, Exception e) {
        //LOGGER.error("write to hdfs error :".concat(StringUtils.toString(body, 0, Math.min(1024, body.length))), e);
        parseError.increment();
    }

    public void writeToHDFS(List<MessageItem> items, byte[] body) {
        if (items != null && !items.isEmpty()) {
            ListIterator<MessageItem> it = items.listIterator();
            while (it.hasNext()) {
                MessageItem item = it.next();
                CallStackV1 callStack = item.getCallStack();

                if (CallStackUtil.validate(callStack)) {
                    CallStackUtil.removeClientAppId(callStack);
                } else {
                    it.remove();
                    metricsService.invalidCallStack(CHECK_EXCEPTION, callStack.getAppId());
                }
            }
            if (!items.isEmpty()) {
                long start = System.currentTimeMillis();
                component.dispatchAll("", new MessageBlock(items, body));
                hdfsTimer.record(Duration.ofMillis(System.currentTimeMillis() - start));
            }
        }
    }

    public List<MessageItem> decode(byte[] compressData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressData);

        try (DataInputStream in = new DataInputStream(new SnappyInputStream(bais))) {
            List<MessageItem> messageItems = newArrayList();
            int offset = 0;
            while (in.available() > 0) {
                int dataLen = in.readInt();
                byte[] data = new byte[dataLen];
                in.readFully(data);
                CallStackV1 callStack = JSONCodecV1.decodeToV1FromArrayFormatTo(data);

                MessageItem item = new MessageItem(callStack);

                //set CallStack offset in message block
                item.setOffset(offset);

                messageItems.add(item);

                offset += 4 + dataLen;
            }
            return messageItems;
        }
    }
}
