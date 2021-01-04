package io.etrace.collector.service.impl;

import io.etrace.collector.service.ClientConfigurationService;
import io.etrace.common.message.agentconfig.MetricConfig;
import io.etrace.common.message.agentconfig.TraceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class ClientConfigurationImpl implements ClientConfigurationService {

    @Autowired
    private DefaultTraceConfig agentConfig;
    @Autowired
    private DefaultMetricConfig metricConfig;

    @Override
    public TraceConfig getAgentConfig(String appId, String hostIp) {
        return agentConfig;
    }

    @Override
    public MetricConfig getMetricConfig(String appId, String hostIp) {
        return metricConfig;
    }

    @Override
    public boolean getTcpConfig(String appId) {
        return false;
    }

    @Component
    @ConfigurationProperties(prefix = "agent")
    @PropertySource("classpath:conf/agent.properties")
    public static class DefaultTraceConfig extends TraceConfig {
    }

    @Component
    @ConfigurationProperties(prefix = "metric")
    @PropertySource("classpath:conf/agent.properties")
    public static class DefaultMetricConfig extends MetricConfig {
    }
}
