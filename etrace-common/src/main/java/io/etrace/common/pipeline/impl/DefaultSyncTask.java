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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class DefaultSyncTask extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSyncTask.class);

    public DefaultSyncTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
    }

    @Override
    public void init(Object... param) {

    }

    @Override
    public void handleEvent(Object key, Object event) {
        try {
            processEvent(key, event);
        } catch (Throwable ex) {
            LOGGER.error("process event error", ex);
        }
    }

    public void processEvent(Object key, Object event) throws Exception {
        component.dispatch(key, event);
    }
}
