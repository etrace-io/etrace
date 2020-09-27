package io.etrace.common.event;

public class MutableEvent {
    private long timestamp;
    private Object key;
    private Object event;

    public Object getKey() {
        return key;
    }

    public Object getEvent() {
        return event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setEvent(Object key, Object event) {
        this.key = key;
        this.event = event;
        this.timestamp = System.currentTimeMillis();
    }

    public void clear() {
        this.event = null;
        this.key = null;
    }
}
