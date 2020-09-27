package io.etrace.collector.cluster.discovery.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.etrace.collector.cluster.discovery.InstanceSerializer;
import io.etrace.collector.cluster.discovery.ServiceInstance;
import io.etrace.collector.cluster.discovery.ServiceProvider;
import io.etrace.common.util.ThreadUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class ServiceDiscovery {
    public final String basePath = "/clusters";
    private final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);
    /**
     * key is cluster name or service name
     */
    private final ConcurrentMap<String, Set<ServiceInstance>> instances = Maps.newConcurrentMap();
    @Autowired
    private ServiceProvider serviceProvider;
    @Autowired
    private InstanceSerializer serializer;
    private volatile boolean running = false;
    private ConnectionStateListener listener;
    private Set<ServiceInstance> currentInstances = Sets.newHashSet();

    public void changeState(ConnectionState connectionState) {
        if (connectionState == ConnectionState.RECONNECTED) {
            LOGGER.info("Re-registering with ZK.");
            try {
                unregister();
                for (ServiceInstance instance : currentInstances) {
                    this.register(instance);
                }
            } catch (Exception e) {
                LOGGER.error("Exception recreating path", e);
                throw new RuntimeException(e);
            }
        }
    }

    @PostConstruct
    public void startup() {
        try {
            this.listener = (curatorFramework, connectionState) -> changeState(connectionState);
            serviceProvider.getClient().getConnectionStateListenable().addListener(listener);
            TreeCache clusterCache = new TreeCache(serviceProvider.getClient(), basePath);
            clusterCache.getListenable().addListener(new ClusterStateListener());
            clusterCache.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        this.running = true;
    }

    @PreDestroy
    public void stop() throws Exception {
        this.running = false;
        serviceProvider.getClient().getConnectionStateListenable().removeListener(listener);
        this.unregister();
    }

    public void register(ServiceInstance instance) throws Exception {
        // Ensure the parent paths exist persistently
        checkParentPathExists(instance);

        final int MAX_TRIES = 2;
        String path = pathForInstance(instance);
        for (int i = 0; i < MAX_TRIES; ++i) {
            try {
                serviceProvider.deleteNode(path);
                serviceProvider.createEphemeralNode(path, serializer.serialize(instance));

                currentInstances.add(instance);
                LOGGER.info("Register success [cluster:{},ip:{},port:{}]", instance.getName(), instance.getAddress(),
                    instance.getPort());
                break;
            } catch (Exception e) {
                LOGGER.error("register failed:", e);
                ThreadUtil.sleep(1);
            }
        }
    }

    public void unregister() {
        currentInstances.forEach(this::unregister);
        for (ServiceInstance instance : currentInstances) {
            Set<ServiceInstance> instanceSet = instances.get(instance.getName());
            if (null != instanceSet && instanceSet.size() > 0) {
                instanceSet.remove(instance);
            }
        }
        currentInstances.clear();
    }

    private void checkParentPathExists(ServiceInstance instance) throws Exception {
        // Ensure the parent paths exist persistently
        serviceProvider.createPersistNode(basePath);
        String clusterPath = basePath + "/" + instance.getName();
        serviceProvider.createPersistNode(clusterPath);
    }

    public void unregister(ServiceInstance instance) {
        String path = pathForInstance(instance);
        final int MAX_TRIES = 2;
        for (int i = 0; i < MAX_TRIES; ++i) {
            try {
                serviceProvider.deleteNode(path);
                LOGGER.info("remove instance:[{}] success!", instance);
                return;
            } catch (Exception e) {
                LOGGER.error("remove instance:[{}] throw a exception:", instance, e);
                ThreadUtil.sleep(1);
            }
        }
    }

    public boolean updateInstance(String name, ServiceInstance instance) {
        Set<ServiceInstance> serviceInstances = instances.get(name);
        if (null == serviceInstances || serviceInstances.isEmpty()) {
            return false;
        }
        return serviceInstances.contains(instance) && serviceInstances.add(instance);
    }

    public Set<ServiceInstance> queryForInstances(String name) {
        Set<ServiceInstance> serviceInstances = instances.get(name);
        if (null == serviceInstances || serviceInstances.isEmpty()) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<ServiceInstance> builder = ImmutableSet.builder();
        builder.addAll(serviceInstances.stream().filter(ServiceInstance::isEnabled).collect(Collectors.toSet()));
        return builder.build();
    }

    public Map<String, Set<ServiceInstance>> getAllInstances() {
        ImmutableMap.Builder<String, Set<ServiceInstance>> builder = ImmutableMap.builder();
        for (Map.Entry<String, Set<ServiceInstance>> entry : instances.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private String pathForInstance(ServiceInstance instance) {
        return basePath + "/" + instance.getName() + "/" + instance.getAddress() + ":" + instance.getPort();
    }

    public synchronized void addInstance(ServiceInstance instance) {
        Set<ServiceInstance> collectorMap = instances.computeIfAbsent(instance.getName(), k -> new HashSet<>());
        collectorMap.add(instance);
    }

    public synchronized void removeInstance(ServiceInstance instance) {
        Set<ServiceInstance> collectorSet = instances.computeIfAbsent(instance.getName(), k -> new HashSet<>());
        collectorSet.remove(instance);
    }

    public void updateListener(TreeCacheEvent event) throws Exception {
        String path = event.getData().getPath();
        ServiceInstance instance = serializer.deserialize(event.getData().getData());
        switch (event.getType()) {
            case NODE_ADDED:
                LOGGER.info("Collector node added: {}.", path);
                addInstance(instance);
                break;
            case NODE_REMOVED:
                LOGGER.info("Collector node removed: {}.", path);
                removeInstance(instance);
                break;
        }
    }

    class ClusterStateListener implements TreeCacheListener {

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            if (running) {
                if (event == null || event.getData() == null) {
                    return;
                }
                String path = event.getData().getPath();
                if (basePath.equalsIgnoreCase(path) || path.split("/").length < 4) {
                    LOGGER.info("change node type:[{}] for path:[{}]", event.getType(), path);
                    return;
                }
                updateListener(event);
            }
        }
    }
}
