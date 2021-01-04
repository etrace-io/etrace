package io.etrace.agent.springautoconfiguration;

import com.google.common.base.Strings;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.autoconfigure.EtraceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EtraceAgentConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtraceAgentConfiguration.class);

    @Autowired
    private EtraceConfig etraceConfig;

    @PostConstruct
    private void initAgentConfig() {
        if (etraceConfig == null || Strings.isNullOrEmpty(etraceConfig.getAppId()) || Strings.isNullOrEmpty(
            etraceConfig.getBackendAddress())) {
            LOGGER.warn(
                "ETrace agent auto configuration fail, you need config `etrace.appId` and `etrace.backendAddress` in "
                    + "`application.yaml`.");
        } else {
            LOGGER.info("ETrace agent auto configuration success. AppId: [{}], BackendAddress: [{}]",
                etraceConfig.getAppId(),
                etraceConfig.getBackendAddress());
            AgentConfiguration.setAppId(etraceConfig.getAppId());
            AgentConfiguration.setCollectorIp(etraceConfig.getBackendAddress());
        }
    }
}
