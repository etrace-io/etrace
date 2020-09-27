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

import java.util.Map;

public abstract class Task {
    protected String name;
    protected Map<String, Object> params;
    protected Component component;
    private volatile boolean running = false;

    public Task(String name, Component component, Map<String, Object> params) {
        this.name = name;
        this.component = component;
        this.params = params;
    }

    public abstract void init(Object... param);

    public abstract void handleEvent(Object key, Object event);

    public String getName() {
        return name;
    }

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