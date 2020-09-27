package io.etrace.collector.service;

import io.etrace.common.queue.PersistentQueue;

public interface PersistentQueueProvider<T> {

    PersistentQueue<T> getPersistentQueue(String name, int idx);
}
