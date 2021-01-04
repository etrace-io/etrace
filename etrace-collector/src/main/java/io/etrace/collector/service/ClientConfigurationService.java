package io.etrace.collector.service;

import io.etrace.common.message.agentconfig.MetricConfig;
import io.etrace.common.message.agentconfig.TraceConfig;

public interface ClientConfigurationService {

    TraceConfig getAgentConfig(String appId, String hostIp);

    MetricConfig getMetricConfig(String appId, String hostIp);

    boolean getTcpConfig(String appId);

}
