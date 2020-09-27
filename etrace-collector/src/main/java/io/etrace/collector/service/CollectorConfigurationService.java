package io.etrace.collector.service;

public interface CollectorConfigurationService {

    boolean isForbiddenAppId(String appId);

    boolean isForbiddenMetricName(String appId, String metricName);

    boolean isResetConnIp(String host);
}
