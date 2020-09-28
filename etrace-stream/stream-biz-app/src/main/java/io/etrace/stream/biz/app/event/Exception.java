package io.etrace.stream.biz.app.event;

import io.etrace.stream.biz.app.EventConstant;

import java.util.Map;

public class Exception extends AbstractEvent {
    private String method;
    private String sourceType;

    public Exception(String type, String name, long timestamp, String status, Map<String, String> tags) {
        super(type, name, timestamp, status, tags);
        method = getTag(EventConstant.EXCEPTION_TAG_METHOD);
        sourceType = getTag(EventConstant.EXCEPTION_TAG_SOURCE_TYPE);
    }

    /**
     * app
     */
    @Override
    public String getEventType() {
        return super.getEventType();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
