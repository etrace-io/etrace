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

package io.etrace.common.queue.impl;

import io.etrace.common.queue.PersistentQueue;
import io.etrace.common.queue.QueueCodec;
import io.etrace.common.queue.QueueConfig;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DiskBackedInMemoryBlockingQueue<T> implements PersistentQueue<T> {
    private final Object producerLock = new Object();
    //in memory queue
    private BlockingQueue<T> inMemoryQueue;
    //on disk persistent queue
    private PersistentQueue<T> persistentQueue;
    private AtomicLong overflowCount = new AtomicLong(0);
    private int memoryCapacity;

    public DiskBackedInMemoryBlockingQueue(QueueConfig config, PersistentQueue<T> persistentQueue) {
        this.persistentQueue = persistentQueue;
        memoryCapacity = config.getMemoryCapacity();
        inMemoryQueue = new ArrayBlockingQueue<>(memoryCapacity);
    }

    @Override
    public void setQueueCodec(QueueCodec codec) {
        this.persistentQueue.setQueueCodec(codec);
    }

    @Override
    public boolean produce(T data) {
        boolean success = inMemoryQueue.offer(data);
        if (!success) {
            overflowCount.incrementAndGet();
            success = persistentQueue.produce(data);
            if (success) {
                synchronized (producerLock) {
                    producerLock.notify();
                }
            }
        }
        return success;
    }

    @Override
    public T consume() {
        T data = null;
        try {
            data = this.inMemoryQueue.poll(5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // todo: 确认下，ignore InterruptedException 是否ok
            // ignore
        }
        if (data == null) {
            data = this.persistentQueue.consume();
        }
        return data;
    }

    @Override
    public long remainingCapacity() {
        return memoryCapacity - inMemoryQueue.size();
    }

    @Override
    public long capacity() {
        return memoryCapacity;
    }

    @Override
    public long usedSize() {
        return inMemoryQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inMemoryQueue.isEmpty() && this.persistentQueue.isEmpty();
    }

    @Override
    public void shutdown() {
        T data;
        try {
            data = this.inMemoryQueue.poll(1, TimeUnit.MILLISECONDS);
            while (null != data) {
                this.persistentQueue.produce(data);
                data = this.inMemoryQueue.poll(1, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ignored) {
        }

        this.persistentQueue.shutdown();
    }

    @Override
    public long getOverflowCount() {
        return overflowCount.getAndSet(0);
    }

    @Override
    public int getBackFileSize() {
        return this.persistentQueue.getBackFileSize();
    }
}
