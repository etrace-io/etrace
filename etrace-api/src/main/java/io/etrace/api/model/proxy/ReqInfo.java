package io.etrace.api.model.proxy;

import lombok.Data;

@Data
@Deprecated
public class ReqInfo {
    private String proxyPath;
    private String rule;
    private String cluster;

}
