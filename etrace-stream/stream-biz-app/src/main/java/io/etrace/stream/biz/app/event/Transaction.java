package io.etrace.stream.biz.app.event;

import java.util.Map;

public class Transaction extends AbstractTransaction {

    public Transaction(String type, String name, long timestamp, String status, Map<String, String> tags,
                       long duration) {
        super(type, name, timestamp, status, tags, duration);
        setType(simplify(type));
        setName(simplify(name));
    }
}
