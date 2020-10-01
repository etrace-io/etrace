package io.etrace.stream.biz.app.event;

import java.util.Map;

public class AbstractDependencyTransaction extends AbstractTransaction {

    private String soaServiceMethod;
    private String rmqConsumerQueue;

    public AbstractDependencyTransaction(String type, String name, long timestamp, String status,
                                         Map<String, String> tags, long duration, String soaServiceMethod,
                                         String rmqConsumerQueue) {
        super(type, name, timestamp, status, tags, duration);
        this.soaServiceMethod = soaServiceMethod;
        this.rmqConsumerQueue = rmqConsumerQueue;
    }

    public String getSoaServiceMethod() {
        return soaServiceMethod;
    }

    public String getRmqConsumerQueue() {
        return rmqConsumerQueue;
    }
}