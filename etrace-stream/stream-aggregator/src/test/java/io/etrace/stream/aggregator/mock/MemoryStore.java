package io.etrace.stream.aggregator.mock;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryStore {
    public static ConcurrentHashMap<String, Object> events = new ConcurrentHashMap<>();

    private MemoryStore() {

    }

    public static void clear() {
        events.clear();
    }

    public static void addEvent(String key, Object event) {
        //System.out.println("MemoryStore:addEvent: " + key + "\t" + event);
        events.put(key, event);
    }

    public static Object getEvent(String key) {
        return events.get(key);
    }
}
