package io.etrace.plugins.prometheus.pushgateway;

import io.etrace.agent.config.AgentConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableWebMvc
public class Application {

    @Value("${etrace.collector}")
    private String collector;
    @Value("${etrace.appId}")
    private String appId;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void startup() {
        initAgentConfig();
    }

    private void initAgentConfig() {
        AgentConfiguration.setAppId(appId);
        AgentConfiguration.setCollectorIp(collector);
    }
}