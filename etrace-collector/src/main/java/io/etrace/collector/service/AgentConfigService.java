package io.etrace.collector.service;

import java.util.Collection;

public interface AgentConfigService {
    String defaultAgentConfig = "{\"configKey\":\"default\",\"enabled\":true,\"aopEnabled\":true,"
        + "\"tagCount\":8,"
        + "\"tagSize\":128,\"dataSize\":256,\"longConnection\":true}";
    String defaultMetricConfig
        = "{\"enabled\":true,\"tagCount\":8,\"tagSize\":256,\"maxPackageCount\":1000,\"maxMetric\":1000,"
        + "\"maxGroup\":10000,\"maxHistogramGroup\":1000,\"aggregatorTime\":1000}";

    String getAgentConfig(String appId, String hostIp);

    boolean getTcpConfig(String appId);

    Collection<String> getAllAgentConfig();

    String getMetricConfig(String appId, String hostIp);
}
