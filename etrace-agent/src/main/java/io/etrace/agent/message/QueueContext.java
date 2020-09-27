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

package io.etrace.agent.message;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.util.NetworkInterfaceHelper;

import java.util.Map;

public class QueueContext<E> {
    private volatile boolean active = true;
    private String hostIp;
    private String hostName;
    private Map<String, String> extraProperties;

    private Disruptor<E> disruptor;
    private RingBuffer<E> ringBuffer;

    public QueueContext() {
        hostIp = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
        hostName = NetworkInterfaceHelper.INSTANCE.getLocalHostName();
        extraProperties = AgentConfiguration.getExtraProperties();
    }

    public void build(String name, int bufferSize, final EventHandler handler, EventFactory<E> factory) {
        // Construct the Disruptor
        disruptor = new Disruptor<>(factory, bufferSize, r -> {
            Thread t = new Thread(r);
            t.setName(name);
            t.setDaemon(true);
            return t;
        }, ProducerType.MULTI, new LiteBlockingWaitStrategy());
        disruptor.handleEventsWith(handler);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<E>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, E event) {

            }

            @Override
            public void handleOnStartException(Throwable ex) {

            }

            @Override
            public void handleOnShutdownException(Throwable ex) {

            }
        });
        ringBuffer = disruptor.getRingBuffer();
        disruptor.start();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getHostIp() {
        return hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public Disruptor<E> getDisruptor() {
        return disruptor;
    }

    public RingBuffer<E> getRingBuffer() {
        return ringBuffer;
    }

    public int getQueueSize() {
        return ringBuffer.getBufferSize() - (int)ringBuffer.remainingCapacity();
    }

    public Map<String, String> getExtraProperties() {
        return extraProperties;
    }
}
