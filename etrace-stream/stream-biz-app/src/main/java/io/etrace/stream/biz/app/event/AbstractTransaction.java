package io.etrace.stream.biz.app.event;

import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public abstract class AbstractTransaction extends AbstractEvent {
    private long duration;

    public AbstractTransaction(String type, String name, long timestamp, String status, Map<String, String> tags,
                               long duration) {
        super(type, name, timestamp, status, tags);
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

}
