package io.etrace.stream.core.model;

import io.etrace.common.constant.Constants;
import lombok.Data;

@Data
public class Header {
    private String entry;
    private String appId;
    private String hostIp;
    private String hostName;
    private String id;
    private String msg;
    private String cluster = Constants.UNKNOWN;
    private String ezone = Constants.UNKNOWN;
    private String idc = Constants.UNKNOWN;
    private String requestId;
    private int rpcLevel;
    private String instance;
}
