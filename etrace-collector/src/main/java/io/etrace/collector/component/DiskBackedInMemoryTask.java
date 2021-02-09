package io.etrace.collector.component;

import com.fasterxml.jackson.core.JsonParseException;
import io.etrace.collector.config.CollectorProperties;
import io.etrace.collector.metrics.MetricsService;
import io.etrace.collector.model.BinaryPairCodec;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.Task;
import io.etrace.common.queue.PersistentQueue;
import io.etrace.common.queue.QueueConfig;
import io.etrace.common.queue.impl.MappedFileQueue;
import io.etrace.common.util.Pair;
import io.etrace.common.util.ThreadUtil;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.etrace.collector.service.impl.PersistentQueueImpl.INCOMING_QUEUE;
import static io.etrace.common.constant.InternalMetricName.TASK_PROCESS_DURATION;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class DiskBackedInMemoryTask extends Task implements Runnable {
    public static final int FLUSH_INTERVAL = 3 * 1000;
    public final Logger LOGGER = LoggerFactory.getLogger(DiskBackedInMemoryTask.class);
    private final Object producerLock = new Object();
    private final AtomicLong overflowCount = new AtomicLong(0);
    private final Timer processTimer;
    @Autowired
    private CollectorProperties collectorProperties;
    //in memory queue
    private BlockingQueue<Pair<MessageHeader, byte[]>> inMemoryQueue;
    //on disk persistent queue
    private PersistentQueue<Pair<MessageHeader, byte[]>> persistentQueue;
    private volatile boolean running = false;
    private long lastFlushTime = System.currentTimeMillis();
    private Thread consumer;
    @Autowired
    private MetricsService metricsService;

    public DiskBackedInMemoryTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        processTimer = Timer.builder(TASK_PROCESS_DURATION)
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .tag("task", name)
            .register(Metrics.globalRegistry);
    }

    @Override
    public void init(Object... param) {
        int idx = Short.parseShort(name.split("-")[2]);
        //MemQueue
        int memoryCapacity = (int)Optional.ofNullable(params.get("bufferSize")).orElse(128);
        this.inMemoryQueue = new ArrayBlockingQueue<>(memoryCapacity);

        //DiskQueue
        QueueConfig queueConfig = new QueueConfig();
        queueConfig.setMaxFileSize(collectorProperties.getQueue().getMaxFileSize() * 1024);
        queueConfig.setRootPath(collectorProperties.getQueue().getPath());
        queueConfig.setName(name);
        queueConfig.setIdx(idx);

        this.persistentQueue = new MappedFileQueue<>(INCOMING_QUEUE, queueConfig);
        this.persistentQueue.setQueueCodec(new BinaryPairCodec());

        running = true;

        consumer = new Thread(this);
        consumer.setName(name);
        consumer.start();
    }

    @Override
    public void handleEvent0(Object key, Object event) {
        MessageHeader messageHeader = (MessageHeader)key;
        Pair<MessageHeader, byte[]> pair = new Pair<>(messageHeader, (byte[])event);
        boolean success = inMemoryQueue.offer(pair);
        if (!success) {
            overflowCount.incrementAndGet();
            success = persistentQueue.produce(pair);
            if (success) {
                synchronized (producerLock) {
                    producerLock.notify();
                }
            }
        }
    }

    @Override
    public void run() {
        while (this.running) {
            String appId = null;
            long startProcessTime = System.currentTimeMillis();
            try {
                Pair<MessageHeader, byte[]> pair = inMemoryQueue.poll(5, TimeUnit.MILLISECONDS);
                if (null != pair) {
                    appId = pair.getKey().getAppId();
                    process(pair);
                }
                flushIfNeed();
            } catch (JsonParseException jpe) {
                LOGGER.error("appId:[{}] to json error!", appId, jpe);
                this.metricsService.messageError(name, appId, "JsonParseException");
            } catch (OutOfMemoryError oom) {
                LOGGER.error("Out of memory error, system will exit!", oom);
                System.exit(-1);
            } catch (InterruptedException ie) {
                LOGGER.warn("worker interrupted", ie);
            } catch (Throwable e) {
                LOGGER.error("appId:[{}],Encounter an error while working", appId, e);
                this.metricsService.messageError(name, appId, e.getMessage());
            } finally {
                //Processing delay
                this.processTimer.record(Duration.ofMillis(System.currentTimeMillis() - startProcessTime));
            }
        }
    }

    private void flushIfNeed() throws Exception {
        if (System.currentTimeMillis() - lastFlushTime > FLUSH_INTERVAL) {
            checkIfNeedFlush();
            lastFlushTime = System.currentTimeMillis();
        }
    }

    public abstract void checkIfNeedFlush() throws Exception;

    protected abstract void flush();

    public abstract void process(Pair<MessageHeader, byte[]> pair) throws Exception;

    public long getOverflowCount() {
        return overflowCount.get();
    }

    @Override
    public void stop() {
        this.running = false;
        int tryCount = 0;
        while (consumer.isAlive() && tryCount < 10) {
            ThreadUtil.sleep(1);
            tryCount++;
        }
        persistentQueue.shutdown();
        flush();
    }
}
