package io.etrace.collector.service;

public interface TrueMetricsService {

    public void defaultHandlerThroughput(int size);

    public void defaultHandlerError(int count);

    public void defaultHandlerforbiddenThoughPut(String appId, long size);

    public void defaultHandlerforbiddenMetricThoughPut(String appId, long size);

    public void agentThoughPut(String appId, String messageType, long size);

    public void agentLatency(String appId, long latency, String messageType, String serverType);

}
