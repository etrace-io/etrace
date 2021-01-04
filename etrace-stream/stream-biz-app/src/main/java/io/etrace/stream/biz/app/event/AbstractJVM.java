package io.etrace.stream.biz.app.event;

import io.etrace.stream.core.model.Event;
import io.etrace.stream.core.model.Header;

public abstract class AbstractJVM implements Event {
    private Header header;
    private String type;
    private String name;
    private long timestamp;
    private double value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public Header getHeader() {
        return this.header;
    }

    @Override
    public void setHeader(Header header) {
        this.header = header;
    }

    @Override
    public String shardingKey() {
        return header.getHostName();
    }

    @Override
    public String getEventType() {
        return "app";
    }

    @Override
    public String getAppId() {
        return header.getAppId();
    }
}
