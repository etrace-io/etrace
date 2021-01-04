package io.etrace.stream.biz.app;

import io.etrace.common.pipeline.Component;
import io.etrace.stream.core.codec.AbstractSnappyDecodeTask;
import io.etrace.stream.core.model.Event;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CallStackTask extends AbstractSnappyDecodeTask {
    private final static String DECODE_CLASS = "decode";
    private CallStackDecode decode;

    @SuppressWarnings("unchecked")
    public CallStackTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        try {
            Class<? extends CallStackDecode> decodeClazz =
                (Class<? extends CallStackDecode>)Class.forName(String.valueOf(params.get(DECODE_CLASS)));
            decode = decodeClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find decode implement.", e);
        }
    }

    @Override
    public String getDecodeType() {
        return "callstack";
    }

    @Override
    public void decode(byte[] data) throws Exception {
        List<Event> events = decode.decode(data);
        if (events != null) {
            for (Event event : events) {
                if (isTsValid(event.getTimestamp())) {
                    component.dispatchAll(event.shardingKey(), event);
                }
            }
        }
    }

}
