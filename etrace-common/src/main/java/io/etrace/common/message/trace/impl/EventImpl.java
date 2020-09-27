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

import com.google.common.base.Strings;
import io.etrace.common.message.trace.AbstractMessage;
import io.etrace.common.message.trace.Event;
import io.etrace.common.message.trace.TraceManager;
import io.etrace.common.util.MessageHelper;

import java.io.PrintWriter;
import java.io.StringWriter;

public class EventImpl extends AbstractMessage implements Event {
    private String data;
    private String message;
    private Throwable t;

    public EventImpl() {
    }

    public EventImpl(String type, String name) {
        this(type, name, null);
    }

    public EventImpl(String type, String name, TraceManager manager) {
        super(type, name, manager);
    }

    public EventImpl(String type, String name, String data, TraceManager manager) {
        super(type, name, manager);
        this.data = data;
    }

    // for delay the throwable to string
    public EventImpl(String type, String name, String message, Throwable t, TraceManager manager) {
        super(type, name, manager);
        this.t = t;
        this.message = message;
    }

    private static String extractDetailedMessage(String message, Throwable t) {
        StringWriter writer = new StringWriter(2048);
        if (!Strings.isNullOrEmpty(message)) {
            writer.write(message);
            writer.write(' ');
        }
        t.printStackTrace(new PrintWriter(writer));
        String detailMessage = writer.toString();
        return detailMessage;
    }

    @Override
    public String getData() {
        if (t != null) {
            data = extractDetailedMessage(message, t);
            // extract data only once
            t = null;
        }
        return data;
    }

    @Override
    public void setData(String data) {
        try {
            if (manager != null) {
                int dataSize = manager.getConfigManager().getDataSize();
                this.data = MessageHelper.truncate(data, dataSize);
            } else {
                this.data = data;
            }
        } catch (Exception e) {
            this.data = data;
        }
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

    @Override
    public String toString() {
        return "EventImpl{" +
            "data='" + data + '\'' +
            ", message='" + message + '\'' +
            ", t=" + t +
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
