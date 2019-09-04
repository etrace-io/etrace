package io.etrace.collector.rest;

import io.etrace.collector.service.AgentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/agent-config/")
public class AgentConfigResource {

    @Autowired
    private AgentConfigService agentConfigService;

    @GetMapping
    public String getAgentConfig(@RequestParam("appId") String appId, @RequestParam("host") String host) {
        return agentConfigService.getAgentConfig(appId, host);
    }

    @GetMapping("/all")
    public Collection<String> getAllAgentConfig() {
        return agentConfigService.getAllAgentConfig();
    }
}
