package io.etrace.collector.util;

import io.etrace.common.util.NetworkInterfaceHelper;

/**
 * @author lizun
 *         Date: 17/2/23
 *         Time: 下午1:28
 */
public interface CollectorConstants {
    String APPID = "me.ele.arch.etrace.collector";

    String EXECUTE_PERIOD_KEY = "execute.period";
    int EXECUTE_ONCE_5_MIN = 5;

    String API_GATEWAY_URL_KEY = "api.gateway.url";

    String SPECIAL = ";";

    String HOST_NAME = NetworkInterfaceHelper.INSTANCE.getLocalHostName();
    String HOST_IP = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();

    String KEY_TCP_PORT = "network.tcp.port";
    String KEY_THRIFT_PORT = "network.thrift.port";
    String KEY_UDP_PORT = "network.udp.port";
    
    String KEY_HTTP_PORT = "network.http.port";

}
