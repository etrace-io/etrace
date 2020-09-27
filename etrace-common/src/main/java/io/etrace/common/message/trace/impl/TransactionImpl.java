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

import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.*;

import java.util.ArrayList;
import java.util.List;

public class TransactionImpl extends AbstractMessage implements Transaction {
    private long duration = 0;
    private boolean isBadTransaction = false;
    private List<Message> children = new ArrayList<>();

    public TransactionImpl() {
    }

    public TransactionImpl(String type, String name) {
        this(type, name, null);
    }

    public TransactionImpl(String type, String name, TraceManager manager) {
        super(type, name, manager);
    }

    @Override
    public void addChild(Message message) {
        if (message == null) {
            return;
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        if (message instanceof Event && Constants.AGENT_EVENT_TYPE.equals(message.getType())
            && Constants.NAME_BAD_TRANSACTION.equals(message.getName())) {
            isBadTransaction = true;
        }
        children.add(message);
    }

    @Override
    public List<Message> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Message> children) {
        this.children = children;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean isBadTransaction() {
        return isBadTransaction;
    }

    @Override
    public void complete() {
        try {
            if (!isCompleted()) {
                duration = System.currentTimeMillis() - timestamp;
                setCompleted(true);
                if (manager != null) {
                    manager.endTransaction(this);
                }
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public String toString() {
        return "TransactionImpl{" +
            "duration=" + duration +
            ", isBadTransaction=" + isBadTransaction +
            ", children=" + children +
            ", type='" + type + '\'' +
            ", name='" + name + '\'' +
            ", status='" + status + '\'' +
            ", timestamp=" + timestamp +
            ", completed=" + completed +
            ", tags=" + tags +
            ", manager=" + manager +
            ", id=" + id +
            '}';
    }
}
