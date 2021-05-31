package io.etrace.collector.service.impl;

import io.etrace.collector.config.CollectorProperties;
import io.etrace.collector.model.BinaryPairCodec;
import io.etrace.collector.service.PersistentQueueProvider;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.queue.PersistentQueue;
import io.etrace.common.queue.QueueConfig;
import io.etrace.common.queue.impl.DiskBackedInMemoryBlockingQueue;
import io.etrace.common.queue.impl.MappedFileQueue;
import io.etrace.common.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersistentQueueImpl implements PersistentQueueProvider<Pair<MessageHeader, byte[]>> {
    public static final String INCOMING_QUEUE = "incoming_queue";

    @Autowired
    private CollectorProperties collectorProperties;
    @Override
    public PersistentQueue<Pair<MessageHeader, byte[]>> getPersistentQueue(String name, int idx) {
        QueueConfig queueConfig = new QueueConfig();
        queueConfig.setMemoryCapacity(collectorProperties.getQueue().getMemoryCapacity());
        queueConfig.setMaxFileSize(collectorProperties.getQueue().getMaxFileSize() * 1024);
        queueConfig.setRootPath(collectorProperties.getQueue().getPath());
        queueConfig.setName(name);
        queueConfig.setIdx(idx);
        PersistentQueue persistentQueue = new MappedFileQueue<>(INCOMING_QUEUE, queueConfig);
        persistentQueue.setQueueCodec(new BinaryPairCodec());
        return new DiskBackedInMemoryBlockingQueue<Pair<MessageHeader, byte[]>>(queueConfig, persistentQueue);
    }
}
