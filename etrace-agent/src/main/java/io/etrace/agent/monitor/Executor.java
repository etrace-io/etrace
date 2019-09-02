package io.etrace.agent.monitor;

import java.util.Map;

public abstract class Executor {

    protected String type;

    public Executor(String type) {
        this.type = type;
    }

    public abstract Map<String, String> execute();
}
