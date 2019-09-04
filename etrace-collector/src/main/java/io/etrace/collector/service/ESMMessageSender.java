package io.etrace.collector.service;

import com.google.inject.Singleton;
import io.etrace.common.modal.MessageHeader;

@Deprecated
@Singleton
public class ESMMessageSender {
    private static final String UDP_UNKNOWN = "UDP_UNKNOWN";

    public ESMMessageSender() {
    }

    public void parseAsMetricV2(String line) throws Exception {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(UDP_UNKNOWN);
        messageHeader.setInstance(UDP_UNKNOWN);
        messageHeader.setHostIp(UDP_UNKNOWN);
        messageHeader.setHostName(UDP_UNKNOWN);

        messageHeader.setKey("INFLUX");
        messageHeader.setCrt(System.currentTimeMillis());
    }

}
