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

import io.etrace.common.pipeline.Component;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class Task {
    protected String name;
    protected Map<String, Object> params;
    protected Component component;
    Timer timer;
    private volatile boolean running = false;

    public Task(String name, Component component, Map<String, Object> params) {
        this.name = name;
        this.component = component;
        this.params = params;
        timer = Timer.builder("task.processTime")
            .tag("component", component.getName())
            .tag("pipeline", component.getPipeline())
            .tag("name", name)
            .register(Metrics.globalRegistry);
    }

    public abstract void init(Object... param);

    public void handleEvent(Object key, Object event) {
        long start = System.currentTimeMillis();
        handleEvent0(key, event);
        timer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
    }

    protected abstract void handleEvent0(Object key, Object event);

    public String getName() {
        return name;
    }

    /**
     * 所有的Task 会被 TaskPool 手动starUp 因此子类，不需要手动自学starup，包括不需要@PostConstruct
     */
    public void startup() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }
}