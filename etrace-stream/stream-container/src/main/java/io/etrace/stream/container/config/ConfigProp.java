package io.etrace.stream.container.config;

import io.etrace.common.pipeline.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties
@Data
public class ConfigProp {
    private List<Resource> resources;
}
