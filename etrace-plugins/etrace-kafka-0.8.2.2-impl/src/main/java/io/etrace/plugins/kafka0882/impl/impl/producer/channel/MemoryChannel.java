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

package io.etrace.plugins.kafka0882.impl.impl.producer.channel;

import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MemoryChannel implements Channel {

    private final int waitTime = 5;
    private ArrayBlockingQueue<RecordInfo> memQueue;
    private int memorySize;

    MemoryChannel(int memorySize) {
        this.memorySize = memorySize;
        this.memQueue = new ArrayBlockingQueue<>(memorySize);
    }

    @Override
    public boolean add(RecordInfo record) throws InterruptedException {
        return memQueue.offer(record, waitTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void put(RecordInfo record) throws InterruptedException {
        memQueue.put(record);
    }

    @Override
    public RecordInfo poll() throws InterruptedException {
        return memQueue.poll(waitTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public RecordInfo take() throws InterruptedException {
        return memQueue.take();
    }

    @Override
    public int getSize() {
        return memQueue.size();
    }

    @Override
    public boolean isFull() {
        return getSize() == this.memorySize;
    }
}
