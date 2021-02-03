package io.etrace.collector.service.impl;

import io.etrace.collector.model.MatchType;
import io.etrace.collector.service.CollectorConfigurationService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CollectorConfigurationImpl implements CollectorConfigurationService {

    @Autowired
    private ForbiddenConfig forbiddenConfig;

    @Override
    public boolean isForbiddenAppId(String appId) {
        return forbiddenConfig.getAppIds().contains(appId);
    }

    @Override
    public boolean isForbiddenMetricName(String appId, String metricName) {
        for (ForbiddenConfig.Metric metric : forbiddenConfig.getMetrics()) {
            boolean matched = metric.getAppId().equalsIgnoreCase(appId) && MatchType.match(metricName, metric.getKey(),
                metric.getType());
            if (matched) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isResetConnIp(String host) {
        return false;
    }

    @Component
    @ConfigurationProperties(prefix = "forbidden")
    @PropertySource("classpath:conf/collector-config.properties")
    @Data
    public static class ForbiddenConfig {
        private Set<String> appIds;
        private List<Metric> metrics;

        @Data
        public static class Metric {
            private String appId;
            private String key;
            private MatchType type;
        }
    }
}
