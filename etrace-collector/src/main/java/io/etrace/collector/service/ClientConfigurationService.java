package io.etrace.collector.service;

public interface ClientConfigurationService {

    String getAgentConfig(String appId, String hostIp);

    String getMetricConfig(String appId, String hostIp);

    boolean getTcpConfig(String appId);

}
