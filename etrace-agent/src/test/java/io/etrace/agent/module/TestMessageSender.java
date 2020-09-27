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

package io.etrace.agent.module;

import com.google.common.collect.Lists;
import io.etrace.agent.io.MessageSender;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMessageSender implements MessageSender {

    List<byte[]> inMemoryQueue = Lists.newCopyOnWriteArrayList();

    AtomicInteger messageTotalCount = new AtomicInteger(0);

    public void clear() {
        inMemoryQueue.clear();
        messageTotalCount.set(0);
    }

    public List<byte[]> getQueue() {
        return inMemoryQueue;
    }

    public int getMessageCount() {
        return messageTotalCount.get();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getQueueSize() {
        return inMemoryQueue.size();
    }

    @Override
    public void send(byte[] chunk, int messageCount) {
        inMemoryQueue.add(chunk);
        messageTotalCount.addAndGet(messageCount);
    }
}
