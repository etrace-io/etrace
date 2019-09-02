package io.etrace.common.modal.impl;

import com.google.common.base.Strings;
import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.AbstractMessage;
import io.etrace.common.modal.Event;
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

    public EventImpl(String type, String name, MessageManager manager) {
        super(type, name, manager);
    }

    public EventImpl(String type, String name, String data, MessageManager manager) {
        super(type, name, manager);
        this.data = data;
    }

    // for delay the throwable to string
    public EventImpl(String type, String name, String message, Throwable t, MessageManager manager) {
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
                    manager.add(this);
                }
            }
        } catch (Exception ignore) {
        }
    }

}
