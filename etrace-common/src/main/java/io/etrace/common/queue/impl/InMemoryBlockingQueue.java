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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBlockingQueue<T> implements PersistentQueue<T> {
    private BlockingQueue<T> inMemoryQueue;
    private AtomicLong overflowCount = new AtomicLong(0);
    private int memoryCapacity;

    public InMemoryBlockingQueue(int memoryCapacity) {
        inMemoryQueue = new ArrayBlockingQueue<>(memoryCapacity);
        this.memoryCapacity = memoryCapacity;
    }

    @Override
    public void setQueueCodec(QueueCodec codec) {
    }

    @Override
    public boolean produce(T data) {
        return inMemoryQueue.offer(data);
    }

    @Override
    public T consume() {
        return this.inMemoryQueue.poll();
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
        return this.inMemoryQueue.isEmpty();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public long getOverflowCount() {
        return overflowCount.getAndSet(0);
    }

    @Override
    public int getBackFileSize() {
        return -1;
    }
}
