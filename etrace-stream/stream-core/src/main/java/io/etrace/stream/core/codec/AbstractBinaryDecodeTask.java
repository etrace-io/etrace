package io.etrace.stream.core.codec;

import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.Map;

public abstract class AbstractBinaryDecodeTask extends DefaultAsyncTask {
    protected final Timer decodeTimer;
    protected final Timer allocationSizeTimer;

    protected AbstractBinaryDecodeTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        String decodeType = getDecodeType();

        decodeTimer = Timer.builder("stream." + decodeType + ".decode")
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .register(Metrics.globalRegistry);

        allocationSizeTimer = Timer.builder("stream." + decodeType + ".allocation.byte")
            .tag("pipeline", component.getPipeline())
            .tag("name", component.getName())
            .register(Metrics.globalRegistry);
    }

    public abstract void process(byte[] keyData, byte[] message) throws Exception;

    @Override
    final public void processEvent(Object key, Object event) throws Exception {
        if (event instanceof byte[]) {
            byte[] keyData = null;
            if (key instanceof byte[]) {
                keyData = (byte[])key;
            }
            process(keyData, (byte[])event);
        }
    }

    // used for metrics
    public abstract String getDecodeType();

}

