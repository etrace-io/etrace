package io.etrace.collector.sharding.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.etrace.collector.cluster.discovery.ServiceInstance;
import io.etrace.collector.cluster.discovery.impl.ServiceDiscovery;
import io.etrace.collector.config.CollectorProperties;
import io.etrace.collector.controller.ThroughputController;
import io.etrace.collector.service.impl.ShardingService;
import io.etrace.collector.sharding.ShardIng;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static io.etrace.collector.controller.ThroughputController.PATH;

@DependsOn("dispatcherServlet")
@Component
public class FrontShardIngImpl implements ShardIng {
    private final Logger LOGGER = LoggerFactory.getLogger(FrontShardIngImpl.class);

    private final LongAdder currentThroughput = new LongAdder();
    private long throughputSnapshot;

    @Autowired
    private CollectorProperties collectorProperties;
    @Autowired
    private ServiceDiscovery serviceDiscovery;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ShardingService shardingService;

    private CollectorProperties.ShardingConfig shardingConfig;
    private ScheduledExecutorService scheduledTask;
    private volatile Map<ServiceInstance, Long> clusterThroughput;

    @PostConstruct
    public void startup() {
        shardingConfig = collectorProperties.getSharding().get("front");

        scheduledTask = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("balance-throughput-thread"));

        scheduledTask.scheduleWithFixedDelay(this::getSnapshot, shardingConfig.getInterval(),
            shardingConfig.getInterval(), TimeUnit.SECONDS);
        scheduledTask.scheduleWithFixedDelay(this::fetchClusterThroughput, shardingConfig.getInterval(),
            shardingConfig.getInterval() * 2, TimeUnit.SECONDS);
    }

    private void getSnapshot() {
        if (shardingConfig.isEnabled()) {
            throughputSnapshot = currentThroughput.sumThenReset() / shardingConfig.getInterval() / 1000;
        }
    }

    private void fetchClusterThroughput() {
        if (shardingConfig.isEnabled()) {
            Set<ServiceInstance> instances = serviceDiscovery.queryForInstances(
                collectorProperties.getCluster().getName());
            if (null == instances || instances.size() < 1) {
                return;
            }

            Map<ServiceInstance, Long> instanceThroughput = Maps.newHashMap();
            for (ServiceInstance instance : instances) {
                try {
                    String url = "http://" + instance.getAddress() + ":" + instance.getHttpPort() + "/" + PATH;
                    ResponseEntity<ThroughputController.ThroughputData> response = restTemplate.getForEntity(url,
                        ThroughputController.ThroughputData.class);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        if (response.getBody().getThroughput() > 0) {
                            instanceThroughput.put(instance, response.getBody().getThroughput());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("fetch instance:[{}] throughput error:", instance, e);
                }
            }
            clusterThroughput = instanceThroughput;
        } else {
            clusterThroughput.clear();
        }
    }

    @PreDestroy
    public void stop() {
        this.scheduledTask.shutdown();
    }

    public void add(long throughput) {
        currentThroughput.add(throughput);
    }

    public long getThroughputSnapshot() {
        return throughputSnapshot;
    }

    public Map<ServiceInstance, Long> getClusterThroughput() {
        return clusterThroughput;
    }

    @Override
    public boolean isEnabled() {
        return shardingConfig.isEnabled();
    }

    public List<ServiceInstance> adjustCollectors(String cluster, List<ServiceInstance> collectors) {
        if (null == clusterThroughput) {
            return collectors;
        }
        double percentage = shardingService.getWeightForFirstLayerSharding(cluster);
        if (percentage >= 1) {
            return collectors;
        }

        List<CollectorThroughput> alternatives = Lists.newArrayList();
        for (ServiceInstance instance : collectors) {
            Long throughput = clusterThroughput.get(instance);
            if (null != throughput) {
                alternatives.add(new CollectorThroughput(instance, throughput));
            }
        }
        //防止发布或者重启期间，instance列表不稳定出现意外情况
        if (alternatives.size() != collectors.size()) {
            return collectors;
        }

        //去除边界
        int pickSize = (int)(alternatives.size() * percentage);
        if (pickSize > alternatives.size() || pickSize < 1) {
            return collectors;
        }

        Collections.sort(alternatives);
        return alternatives.subList(0, pickSize).stream().map(a -> a.instance)
            .collect(Collectors.toList());
    }

    private static class CollectorThroughput implements Comparable<CollectorThroughput> {
        private ServiceInstance instance;
        private long throughput;

        CollectorThroughput(ServiceInstance instance, long throughput) {
            this.instance = instance;
            this.throughput = throughput;
        }

        @Override
        public int compareTo(CollectorThroughput o) {
            return Long.compare(this.throughput, o.throughput);
        }
    }
}
