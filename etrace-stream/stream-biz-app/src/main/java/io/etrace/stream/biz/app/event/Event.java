package io.etrace.stream.biz.app.event;

import java.util.Map;

public class Event extends AbstractEvent {

    public Event(String type, String name, long timestamp, String status, Map<String, String> tags) {
        super(type, name, timestamp, status, tags);
        setType(simplify(type));
        setName(simplify(name));
    }
}
