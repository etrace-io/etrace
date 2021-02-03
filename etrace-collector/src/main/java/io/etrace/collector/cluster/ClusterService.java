package io.etrace.collector.cluster;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.etrace.collector.cluster.discovery.ServiceDiscovery;
import io.etrace.collector.cluster.discovery.ServiceInstance;
import io.etrace.collector.config.CollectorProperties;
import io.etrace.collector.model.MatchType;
import io.etrace.collector.sharding.impl.FrontShardIngImpl;
import io.etrace.common.pipeline.PipelineConfiguration;
import io.etrace.common.pipeline.PipelineRepository;
import io.etrace.common.pipeline.impl.DefaultPipelineLoader;
import io.etrace.common.util.NetworkInterfaceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@DependsOn({"config"})
@Component
public class ClusterService implements BeanFactoryAware {
    private final Logger LOGGER = LoggerFactory.getLogger(ClusterService.class);

    @Autowired
    private CollectorProperties collectorProperties;

    @Value("${server.port}")
    private int httpPort;

    @Autowired
    private ServiceDiscovery serviceDiscovery;
    @Autowired
    private FrontShardIngImpl balanceThroughputService;

    private PipelineRepository pipelineRepository;

    private BeanFactory beanFactory;

    @PostConstruct
    public void start() {
        try {
            // start pipelines
            pipelineRepository = beanFactory.getBean(PipelineRepository.class,
                new DefaultPipelineLoader().load(),
                collectorProperties.getResources());
            pipelineRepository.initAndStartUp();

            // service register and discovery
            List<PipelineConfiguration.Channel> receives = Lists.newArrayList();
            for (PipelineConfiguration pipelineConfiguration : pipelineRepository.getPipelines()) {
                receives.addAll(pipelineConfiguration.getReceivers());
            }
            register(receives);
        } catch (Exception e) {
            LOGGER.error("ClusterService fail to start, will shutdown service", e);
            System.exit(-1);
        }
    }

    public void register(List<PipelineConfiguration.Channel> receives) throws Exception {
        for (PipelineConfiguration.Channel channel : receives) {
            ServiceInstance instance = ServiceInstance.builder().cluster(collectorProperties.getCluster().getName())
                .address(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress())
                .serverType(channel.getType().toString())
                .port((Integer)channel.getProps().get("port"))
                .httpPort(httpPort)
                .enabled(true)
                .build();
            serviceDiscovery.register(instance);
        }
    }

    public List<ServiceInstance> getCollectors(String appId, @Nullable String protocol) {
        // step one: get collector cluster by appId
        String cluster = getClusterByAppId(appId);
        // step two: filter by protocol
        Set<ServiceInstance> instances = serviceDiscovery.queryForInstances(cluster);
        List<ServiceInstance> collectors = instances.stream()
            // 若 protocol为null，则返回所有；否则返回protocol匹配的
            .filter(in -> Strings.isNullOrEmpty(protocol) || in.getServerType().equalsIgnoreCase(protocol))
            .collect(Collectors.toList());
        // step three: adjust by throughput
        if (balanceThroughputService.isEnabled()) {
            collectors = balanceThroughputService.adjustCollectors(cluster, collectors);
        }

        return collectors;
    }

    /**
     * find related collector cluster by appId according `collector.cluster` configuration if not found, fall back to
     * `collector.defaultCluster`.
     */
    private String getClusterByAppId(String appId) {
        try {
            if (!Strings.isNullOrEmpty(appId) && null != collectorProperties.getCluster().getMapping()) {
                for (CollectorProperties.Mapping mapping : collectorProperties.getCluster().getMapping()) {
                    if (!Strings.isNullOrEmpty(mapping.getCluster()) &&
                        MatchType.match(appId, mapping.getAppId(), mapping.getType())) {
                        return mapping.getCluster();
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("error for appId: [{}] to get clusterMapping, fallback to default cluster", appId, e);
        }
        return collectorProperties.getCluster().getDefaultCluster();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
