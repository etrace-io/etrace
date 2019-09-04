package io.etrace.collector.route;

import io.etrace.collector.route.worker.Worker;
import io.etrace.common.modal.MessageHeader;

public interface Route {
    void init() ;

    boolean route(MessageHeader header, byte[] msg);

    void shutdown();

    void startup();

    void clear();

    String getName();

    Worker[] getWorkers();
}
