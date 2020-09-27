package io.etrace.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config")
@Data
public class Config {
    private String appId;
    private String backendAddress;
    private QueueProperties queue;

    @Data
    public static class QueueProperties {
        private String path;
        private int memoryCapacity;
        private int maxFileSize;
    }
}
