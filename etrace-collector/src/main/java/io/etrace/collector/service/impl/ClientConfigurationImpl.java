package io.etrace.collector.service.impl;

import io.etrace.collector.service.ClientConfigurationService;
import io.etrace.common.message.agentconfig.MetricConfig;
import io.etrace.common.message.agentconfig.TraceConfig;
import io.etrace.common.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class ClientConfigurationImpl implements ClientConfigurationService {

    private String defaultAgentConfig;
    private String defaultMetricConfig;
    @Autowired
    private DefaultTraceConfig agentConfig;
    @Autowired
    private DefaultMetricConfig metricConfig;

    @PostConstruct
    public void postConstruct() throws IOException {
        defaultAgentConfig = JSONUtil.toString(agentConfig);
        defaultMetricConfig = JSONUtil.toString(metricConfig);
    }

    @Override
    public String getAgentConfig(String appId, String hostIp) {
        return defaultAgentConfig;
    }

    @Override
    public String getMetricConfig(String appId, String hostIp) {
        return defaultMetricConfig;
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
