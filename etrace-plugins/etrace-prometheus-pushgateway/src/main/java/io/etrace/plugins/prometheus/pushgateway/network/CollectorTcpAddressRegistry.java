package io.etrace.plugins.prometheus.pushgateway.network;


import io.etrace.common.message.agentconfig.Collector;
import io.etrace.common.message.agentconfig.CollectorItem;
import io.etrace.common.util.NetworkInterfaceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CollectorTcpAddressRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorTcpAddressRegistry.class);

    private String collectorAddress;

    private RestTemplate restTemplate;

    private static CollectorTcpAddressRegistry instance;


    private static AtomicLong collectorIndex = new AtomicLong(0);

    private static List<Collector> collectorList = Collections.emptyList();

    private String appId;


    public static CollectorTcpAddressRegistry getInstance() {
        return instance;
    }


    public synchronized static void build(String address, String appId) {
        if (null != instance) {
            return;
        }
        instance = new CollectorTcpAddressRegistry(address, appId);
    }

    private CollectorTcpAddressRegistry(String address, String appId) {
        this.collectorAddress = address;
        this.appId = appId;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(requestFactory);
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(() -> pollCollectorItemList(), 0, 1, TimeUnit.MINUTES);

    }


    private void pollCollectorItemList() {
        try {
            String collectorUrl = getCollectorAddressUrl();
            CollectorItem collectorItem = getcollectorAddressFromHttp(collectorUrl);
            if (null != collectorItem && !CollectionUtils.isEmpty(collectorItem.getTcpCollector())) {
                collectorList = collectorItem.getTcpCollector();
            }
        } catch (Throwable t) {
            LOGGER.error("poll collector address error", t);
        }
    }



    private String getCollectorAddressUrl() {
        String collecorConfigPollUrl = String.format("http://%s/collector/item?appId=%s&host=%s&hostName=%s",
                collectorAddress,
                appId, NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(),
                NetworkInterfaceHelper.INSTANCE.getLocalHostName());
        return collecorConfigPollUrl;

    }


    private CollectorItem getcollectorAddressFromHttp(String url) {
        CollectorItem collectorItem = null;
        try {
            collectorItem = restTemplate.getForObject(url, CollectorItem.class);
        } catch (Exception e) {
            LOGGER.error("get collector address error", e);
        }
        return collectorItem;
    }

    public List<Collector> getCollectorList() {
        return collectorList;
    }

    public static Collector getTcpCollector() {
        long next = collectorIndex.getAndAdd(1) % collectorList.size();
        return collectorList.get((int) next);
    }

}
