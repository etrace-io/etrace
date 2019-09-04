package io.etrace.collector.rest;

import io.etrace.agent.Trace;
import io.etrace.collector.service.AgentConfigService;
import io.etrace.collector.service.CollectorAddressService;
import io.etrace.common.modal.Collector;
import io.etrace.common.modal.CollectorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@RestController
@RequestMapping("/collector/")
public class CollectorAddressResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(CollectorAddressResource.class);

    @Autowired
    private CollectorAddressService collectorAddressService;
    @Autowired
    private AgentConfigService agentConfigService;

    @Value("${network.thrift.port}")
    private int thriftPort;
    @Value("${network.tcp.port}")
    private int tcpPort;

    // 返回tcp协议地址,目前只用于esm
    @GetMapping("/tcp/cluster")
    public Collection<Collector> getESMCollectors(@RequestParam("cluster") String cluster) {
        // all port
        List<Collector> collectors = collectorAddressService.getCollectorsForCluster(cluster);

        List<Collector> tcpCollector = newArrayList();
        collectors.forEach(collector -> {
            if (collector.getPort() == tcpPort) {
                tcpCollector.add(collector);
            }
        });
        //return tcp collector
        Collections.shuffle(tcpCollector);
        return tcpCollector;
    }

    // 返回thrift协议地址，用于老版本
    @GetMapping
    public Collection<Collector> getCollectors(@RequestParam("appId") String appId, @RequestParam("host") String host) {
        //thrift port
        List<Collector> collectors = getThriftPort(collectorAddressService.getCollectorsByAppId(host, appId));

        Collections.shuffle(collectors);
        return collectors;
    }

    private List<Collector> getThriftPort(Collection<Collector> collectors) {
        List<Collector> thriftCollector = newArrayList();
        try {
            if (null != collectors && collectors.size() > 0) {
                collectors.forEach(collector -> {
                    if (thriftPort == collector.getPort()) {
                        thriftCollector.add(collector);
                    }
                });
            }
        } catch (Exception e) {
            Trace.logError(e);
        }
        return thriftCollector;
    }

    // 内部查看的接口
    @GetMapping("/all")
    public Collection<Collector> getAllCollectors() {
        return collectorAddressService.getAll();
    }

    // 内部查看的接口
    @GetMapping("/default_cluster")
    public Object internalDefaultCollectors() {
        return collectorAddressService.internalDefaultCollectors();
    }

    // 内部查看的接口
    @GetMapping("/all_cluster")
    public Object internalClusterCollectors() {
        return collectorAddressService.internalClusterCollectors();
    }

    // 即有Tcp又有Thrift协议地址,用于新版本
    @GetMapping("/item")
    public CollectorItem getCollectorItem(@RequestParam("appId") String appId, @RequestParam("host") String host) {
        List<Collector> collectors = collectorAddressService.getCollectorsByAppId(host, appId);
        CollectorItem item = getCollectorItem(collectors, appId);
        item.setTcpCollector(item.getTcpCollector());
        item.setThriftCollector(item.getThriftCollector());
        return item;
    }

    private CollectorItem getCollectorItem(List<Collector> collectors, String appId) {
        CollectorItem item = new CollectorItem();
        try {
            List<Collector> thriftCollector = newArrayList();
            List<Collector> tcpCollector = newArrayList();
            if (null != collectors && collectors.size() > 0) {
                collectors.forEach(collector -> {
                    if (collector.getPort() == tcpPort) {
                        tcpCollector.add(collector);
                    } else if (collector.getPort() == thriftPort) {
                        thriftCollector.add(collector);
                    }
                });
                Collections.shuffle(tcpCollector);
                Collections.shuffle(thriftCollector);
                item.setTcpCollector(tcpCollector);
                item.setThriftCollector(thriftCollector);
                item.setUseTcp(agentConfigService.getTcpConfig(appId));
            }
        } catch (Exception e) {
            Trace.logError(e);
        }
        return item;
    }
}
