package io.etrace.stream.biz.app.event;

public class EventCheck {
    private static EventCheck instance = new EventCheck();
    private final TypeNameCheck typeNameCheck = new TypeNameCheck();

    private EventCheck() {

    }

    public static EventCheck getInstance() {
        return instance;
    }

    public boolean isTooMany(String appId, String type, String name) {
        return typeNameCheck.isTooMany(appId, type, name);
    }
}
