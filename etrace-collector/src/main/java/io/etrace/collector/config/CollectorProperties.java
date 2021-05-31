package io.etrace.collector.config;

import io.etrace.collector.model.MatchType;
import io.etrace.common.pipeline.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "etrace.collector")
@Data
public class CollectorProperties {
    private ClusterConfig cluster;
    private Map<String, ShardingConfig> sharding;
    private List<Resource> resources;
    private QueueProperties queue;

    @Data
    public static class QueueProperties {
        private String path;
        private int memoryCapacity;
        private int maxFileSize;
    }

    @Data
    public static class ClusterConfig {
        private String name;
        private String defaultCluster;
        private String zkPath;
        private Register register;
        private Set<Mapping> mapping;
    }

    @Data
    @EqualsAndHashCode
    public static class Mapping {
        @EqualsAndHashCode.Include
        private String appId;
        private MatchType type = MatchType.EQUAL;
        private String cluster;
    }

    @Data
    public static class Register {
        private String zkAddress;
        private String namespace;
    }

    @Data
    public static class ShardingConfig {
        private int interval;
        private boolean enabled;

    }
}
