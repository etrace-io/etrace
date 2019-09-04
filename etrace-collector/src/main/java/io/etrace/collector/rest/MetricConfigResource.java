package io.etrace.collector.rest;

import io.etrace.collector.service.AgentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metric-config/")
public class MetricConfigResource {

    @Autowired
    public AgentConfigService agentConfigService;

    @GetMapping
    public String getMetricConfig(@RequestParam("appId") String appId, @RequestParam("host") String host) {
        return agentConfigService.getMetricConfig(appId, host);
    }
}
