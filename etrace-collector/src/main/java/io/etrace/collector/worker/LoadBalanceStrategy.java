package io.etrace.collector.worker;

import io.etrace.common.message.trace.MessageHeader;

public interface LoadBalanceStrategy {

    int getWorker(MessageHeader header, int buckets);
}
