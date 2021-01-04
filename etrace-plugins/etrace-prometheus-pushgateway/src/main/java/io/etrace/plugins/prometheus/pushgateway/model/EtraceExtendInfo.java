package io.etrace.plugins.prometheus.pushgateway.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EtraceExtendInfo {
    private String remoteHostName;
    private String remoteHostIp;

}
