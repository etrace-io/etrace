package io.etrace.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config")
@Data
@Deprecated
public class Config {
    private String appId;
    private String backendAddress;

}
