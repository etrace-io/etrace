package io.etrace.common.modal.impl;

import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.AbstractMessage;
import io.etrace.common.modal.Heartbeat;

import java.util.HashMap;
import java.util.Map;

public class HeartbeatImpl extends AbstractMessage implements Heartbeat {
    private String data;

    public HeartbeatImpl() {

    }

    public HeartbeatImpl(String type, String name) {
        this(type, name, null);
    }

    public HeartbeatImpl(String type, String name, MessageManager manager) {
        super(type, name, manager);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

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
                    manager.add(this);
                }
            }
        } catch (Exception ignore) {
        }
    }

}
