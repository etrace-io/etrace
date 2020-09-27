package io.etrace.common.event;

import com.lmax.disruptor.EventFactory;

public class MutableEventFactory implements EventFactory<MutableEvent> {
    @Override
    public MutableEvent newInstance() {
        return new MutableEvent();
    }
}
