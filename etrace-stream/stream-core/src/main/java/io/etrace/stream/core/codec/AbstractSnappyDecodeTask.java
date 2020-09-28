package io.etrace.stream.core.codec;

import io.etrace.common.io.BlockStoreReader;
import io.etrace.common.pipeline.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSnappyDecodeTask extends AbstractBinaryDecodeTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSnappyDecodeTask.class);
    private static final long LAST_TS_LIMIT_IN_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final long FUTURE_TS_LIMIT_IN_MILLIS = TimeUnit.MINUTES.toMillis(30);

    protected AbstractSnappyDecodeTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
    }

    //todo 多算了很多次
    public static boolean isTsValid(long ts) {
        final long current = System.currentTimeMillis();
        final long last = current - LAST_TS_LIMIT_IN_MILLIS;
        final long future = current + FUTURE_TS_LIMIT_IN_MILLIS;

        return ts >= last && ts <= future;
    }

    @Override
    public void process(byte[] keyData, byte[] message) {
        long decodeStart = System.currentTimeMillis();
        for (byte[] data : BlockStoreReader.newSnappyIterator(message)) {
            try {
                decode(data);
            } catch (Exception e) {
                String msg = getDecodeType() + " snappy decode error";
                LOGGER.error(msg + " body:{}", data == null ? "null" : new String(data), e);
            }
        }
        decodeTimer.record(System.currentTimeMillis() - decodeStart, TimeUnit.MILLISECONDS);

    }

    public abstract void decode(byte[] data) throws Exception;

}
