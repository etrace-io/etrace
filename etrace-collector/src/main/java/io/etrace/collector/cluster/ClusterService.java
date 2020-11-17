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

    private String defaultCluster;
    private BeanFactory beanFactory;

    @PostConstruct
    public void start() {
        try {
            this.defaultCluster = collectorProperties.getCluster().getDefaultCluster();

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
            ServiceInstance instance = ServiceInstance.builder().name(collectorProperties.getCluster().getName())
                .address(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress())
                .serverType(channel.getType().toString())
                .port((Integer)channel.getProps().get("port"))
                .httpPort(httpPort)
                .enabled(true)
                .build();
            serviceDiscovery.register(instance);
        }
    }

    public List<ServiceInstance> getCollectors(String appId, String protocol) {
        String cluster = getClusterByAppId(appId);
        Set<ServiceInstance> instances = serviceDiscovery.queryForInstances(cluster);
        List<ServiceInstance> collectors = instances.stream().filter(in -> {
            if (!Strings.isNullOrEmpty(protocol)) {
                return in.getServerType().equalsIgnoreCase(protocol);
            }
            return true;
        }).collect(Collectors.toList());

        if (balanceThroughputService.isEnabled()) {
            collectors = balanceThroughputService.adjustCollectors(cluster, collectors);
        }

        return collectors;
    }

    private String getClusterByAppId(String appId) {
        try {
            if (!Strings.isNullOrEmpty(appId)) {
                if (null != collectorProperties.getCluster().getMapping()) {
                    for (CollectorProperties.Mapping mapping : collectorProperties.getCluster().getMapping()) {
                        if (MatchType.match(appId, mapping.getAppId(), mapping.getType())) {
                            if (!Strings.isNullOrEmpty(mapping.getCluster())) {
                                return mapping.getCluster();
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("error for appId:{},clusterMapping -> {}, fallback...", appId, e);
        }
        return defaultCluster;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
