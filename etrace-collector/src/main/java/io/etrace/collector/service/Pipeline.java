package io.etrace.collector.service;

import io.etrace.common.modal.MessageHeader;

public interface Pipeline {
    void produce(MessageHeader header, byte[] msg);

    void shutdown();

    void startup() throws Exception;
}
