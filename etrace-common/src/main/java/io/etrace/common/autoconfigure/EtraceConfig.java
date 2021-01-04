package io.etrace.common.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "etrace")
public class EtraceConfig {
    private String appId;
    private String backendAddress;
}
