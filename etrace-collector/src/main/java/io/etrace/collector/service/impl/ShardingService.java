package io.etrace.collector.service.impl;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ShardingService {

    @Autowired
    private FirstLayerShardingConfig firstLayerShardingConfig;

    public double getWeightForFirstLayerSharding(String cluster) {
        if (null != firstLayerShardingConfig && firstLayerShardingConfig.getFront().containsKey(cluster)) {
            double weight = Math.abs(Double.parseDouble(firstLayerShardingConfig.getFront().get(cluster)));
            return Math.min(weight, 1);
        }
        return 1;
    }

    @Component
    @ConfigurationProperties(prefix = "sharding")
    // PropertySource 只能处理 .properties文件:
    // https://docs.spring.io/spring-boot/docs/1.5.22.RELEASE/reference/html/boot-features-external-config
    // .html#boot-features-external-config-yaml-shortcomings
    @PropertySource("classpath:conf/collector-config.properties")
    @Data
    public static class FirstLayerShardingConfig {
        private Map<String, String> front;
    }
}
