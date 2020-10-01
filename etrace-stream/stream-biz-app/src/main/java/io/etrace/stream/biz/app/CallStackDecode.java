package io.etrace.stream.biz.app;

import io.etrace.stream.core.model.Event;

import java.io.IOException;
import java.util.List;

public interface CallStackDecode {

    /**
     * Decode CallStack binary to special business event
     *
     * @param data CallStack binary
     * @return event list
     * @throws IOException throw exception when decode error
     */
    List<Event> decode(byte[] data) throws IOException;
}
