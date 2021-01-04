package io.etrace.collector.cluster.discovery;

import io.etrace.collector.config.CollectorProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ServiceProvider {
    private final Logger LOGGER = LoggerFactory.getLogger(ServiceProvider.class);

    @Autowired
    private CollectorProperties collectorProperties;

    private CuratorFramework client;

    @PostConstruct
    public void start() {
        client = CuratorFrameworkFactory.builder()
            .connectString(collectorProperties.getCluster().getRegister().getZkAddress())
            .namespace(collectorProperties.getCluster().getRegister().getNamespace())
            .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
            .connectionTimeoutMs(5000)
            .build();
        client.start();
    }

    @PreDestroy
    public void stop() {
        this.client.close();
        LOGGER.info("zookeeper close success!");
    }

    public void deleteNode(String path) throws Exception {
        if (client.checkExists().forPath(path) != null) {
            client.delete().forPath(path);
        }
    }

    public void createEphemeralNode(String path, byte[] data) throws Exception {
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data);
    }

    //public void createEphemeralNode(String path) throws Exception {
    //    client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
    //}

    public void createPersistNode(String name) throws Exception {
        while (client.checkExists().forPath(name) == null) {
            try {
                client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(name);
            } catch (Throwable e) {
                LOGGER.error("create persist node[{}] error:", name, e);
            }
        }
    }

    public CuratorFramework getClient() {
        return client;
    }
}
