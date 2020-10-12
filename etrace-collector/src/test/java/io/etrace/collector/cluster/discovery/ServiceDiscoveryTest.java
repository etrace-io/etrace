package io.etrace.collector.cluster.discovery;

import io.etrace.collector.BaseTest;
import io.etrace.collector.cluster.ClusterService;
import io.etrace.collector.config.CollectorProperties;
import io.etrace.collector.config.RestTemplateConfig;
import io.etrace.collector.sharding.impl.FrontShardIngImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore("无法正常工作")
@SpringBootTest(classes = {ClusterService.class, CollectorProperties.class,
    ServiceProvider.class, ServiceDiscovery.class,
    InstanceSerializer.class, FrontShardIngImpl.class, RestTemplateConfig.class})
@RunWith(SpringRunner.class)
public class ServiceDiscoveryTest extends BaseTest {

    //    @Autowired
    //    private ServiceProviderImpl serviceProvider;

    @Autowired
    private CollectorProperties collectorProperties;

    @Before
    public void before() {
        startZookeeperServer();
    }

    @Test
    public void start() {
        ServiceInstance.builder().address("xx").build();
    }

}
