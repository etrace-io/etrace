/*
 * Copyright 2020 etrace.io
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

package io.etrace.common.pipeline.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.etrace.common.event.MutableEvent;
import io.etrace.common.event.MutableEventFactory;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.TimeTick;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.etrace.common.constant.InternalMetricName.*;

public abstract class DefaultAsyncTask extends Task implements EventHandler<MutableEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncTask.class);
    protected RingBuffer<MutableEvent> buffer;
    private Disruptor<MutableEvent> disruptor;
    private Timer pendingTimer;
    private Timer processTimer;
    private Counter processErrorCounter;

    public DefaultAsyncTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        pendingTimer = Timer.builder(TASK_PENDING)
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .tag("task", name)
            .register(Metrics.globalRegistry);

        processTimer = Timer.builder(TASK_PROCESS_DURATION)
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .tag("task", name)
            .register(Metrics.globalRegistry);

        processErrorCounter = Counter.builder(TASK_MSG_ERROR)
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .tag("task", name)
            .register(Metrics.globalRegistry);
    }

    @Override
    public void init(Object... param) {
        int bufferSize = (int)Optional.ofNullable(params.get("bufferSize")).orElse(128);
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("Buffer size must be a power of 2.");
        }

        disruptor = new Disruptor<>(new MutableEventFactory(), bufferSize, r -> {
            return new Thread(r, this.name);
        }, ProducerType.MULTI, new LiteBlockingWaitStrategy());

        this.buffer = disruptor.getRingBuffer();
        //add event handler
        disruptor.handleEventsWith(this);

        disruptor.setDefaultExceptionHandler(new ExceptionHandler<MutableEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, MutableEvent event) {
                LOGGER.error("Task <{}> disruptor handle event error:", name, ex);
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                LOGGER.error("Task <{}> disruptor start error:", name, ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                LOGGER.error("Task <{}> disruptor shutdown error:", name, ex);
            }
        });

        int timeTick = -1;
        if (null != params && params.containsKey("timeTick")) {
            timeTick = Integer.parseInt(String.valueOf(params.get("timeTick")));

            try {
                timeTick = Integer.parseInt(String.valueOf(params.get("timeTick")));
                if (timeTick <= 0) {
                    throw new NumberFormatException(timeTick + " <=0");
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid 'timeTick' [{}] configuration for Task [{}]. It should be number or greater "
                    + "than 0!(time unit in milliseconds) System.exit()", params.get("timeTick"), name, e);
                System.exit(1);
            }
        }

        // set up scheduled flush() thread
        if (timeTick > 0) {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder()
                    .setNameFormat(String.format("Component-%s-Timetick-%d-Thread", name, timeTick) + "-%d")
                    .setDaemon(true).build()
            );
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                this.handleEvent(TimeTick.TICK, null);
            }, 1000, timeTick, TimeUnit.MILLISECONDS);
        }

        Gauge.builder(TASK_QUEUE_REMAINING, this.buffer, RingBuffer::remainingCapacity)
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .tag("task", name)
            .register(Metrics.globalRegistry);
    }

    public void onTimeTick() {
        Counter.builder(TASK_TIME_TICK)
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .tag("task", name)
            .register(Metrics.globalRegistry).increment();
    }

    @Override
    public void startup() {
        if (disruptor != null) {
            LOGGER.debug("Starting Disruptor Queue for [{}] [{}] [{}]", this.name, component.getName(),
                this.getClass().getName());
            disruptor.start();
        }
    }

    public void processEvent(Object key, Object event) throws Exception {
        component.dispatchAll(key, event);
    }

    @Override
    public void onEvent(MutableEvent mutableEvent, long l, boolean b) {
        Object event = mutableEvent.getEvent();
        Object key = mutableEvent.getKey();
        long sendTime = mutableEvent.getTimestamp();
        mutableEvent.clear();
        try {
            long startProcessTime = System.currentTimeMillis();
            pendingTimer.record(startProcessTime - sendTime, TimeUnit.MILLISECONDS);
            // if TimeTick
            if (key instanceof TimeTick) {
                onTimeTick();
            } else {
                // process event
                processEvent(key, event);
            }

            processTimer.record(System.currentTimeMillis() - startProcessTime, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            processErrorCounter.increment();
            LOGGER.error("Task <{}> process event", this.name, e);
        }
    }

    @Override
    public void handleEvent0(Object key, Object event) {
        final long sequence = buffer.next();

        final MutableEvent mutableEvent = buffer.get(sequence);
        mutableEvent.setEvent(key, event);
        buffer.publish(sequence);
    }

    @Override
    public void stop() {
        try {
            if (disruptor != null) {
                disruptor.shutdown(10, TimeUnit.SECONDS);
            }
        } catch (Throwable e) {
            LOGGER.error("shutdown task <{}> error:", this.name, e);
        }
        LOGGER.debug("Shutdown task <{}> successfully!", this.name);
    }
}
