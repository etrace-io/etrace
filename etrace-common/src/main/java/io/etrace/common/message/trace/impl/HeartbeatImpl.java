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

package io.etrace.common.message.trace.impl;

import io.etrace.common.message.trace.AbstractMessage;
import io.etrace.common.message.trace.Heartbeat;
import io.etrace.common.message.trace.TraceManager;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
public class HeartbeatImpl extends AbstractMessage implements Heartbeat {
    private String data;

    public HeartbeatImpl() {

    }

    public HeartbeatImpl(String type, String name) {
        this(type, name, null);
    }

    public HeartbeatImpl(String type, String name, TraceManager manager) {
        super(type, name, manager);
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public void addTags(Map<String, String> tags) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.putAll(tags);
    }

    @Override
    public void complete() {
        try {
            if (!isCompleted()) {
                setCompleted(true);
                if (manager != null) {
                    manager.addNonTransaction(this);
                }
            }
        } catch (Exception ignore) {
        }
    }

}
