package io.etrace.api.service;

import io.etrace.agent.Trace;
import io.etrace.common.constant.Constants;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final ConcurrentHashMap blackIpList = new ConcurrentHashMap();

    /**
     * check the ip and the url need to be limited
     *
     * @param ip  the requestIp
     * @param url the requestUri
     * @return
     */
    public boolean checkPermit(String ip, String url) {
        if (blackIpList.contains(ip)) {
            Map tag = new HashMap(1);
            tag.put("ip", ip);
            Trace.logEvent("IpRateLimit", url, Constants.SUCCESS, tag);
            return false;
        }
        return true;
    }

    public void addBlackIp(String blackIp) {
        blackIpList.put(blackIp, null);
    }

}

