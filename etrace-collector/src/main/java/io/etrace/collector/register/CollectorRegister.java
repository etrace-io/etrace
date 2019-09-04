package io.etrace.collector.register;

import com.google.common.base.Preconditions;
import io.etrace.collector.service.ClusterService;
import io.etrace.collector.service.CollectorAddressService;
import io.etrace.common.modal.Collector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;

@Service
public class CollectorRegister {
    private final static Logger LOGGER = LoggerFactory.getLogger(CollectorRegister.class);
    @Autowired
    private CollectorAddressService collectorAddressService;

    @Autowired
    private ClusterService clusterService;

    @Value("${collector.clusters}")
    List<String> clusterList;


    private final static String ZK_CLUSTER_NODE = "/clusters";
    private final static String ZK_BASE_NODE = "/nodes";
    private CuratorFramework framework;
    private ConnectionStateListener listener;

    // 单独出来的集群
    private Set<String> separatedCluster = newHashSet();

    // 当前节点应当注册到zk的path
    // 如果当前节点是默认cluster,则同时注册到/nodes和/clusters
    // 否则只注册到/clusters
    private Set<String> currentZkPath = newHashSet(); // thrift or tcp

    public void startup(CuratorFramework framework) throws Exception {
        this.framework = framework;

        Preconditions.checkArgument(framework.getState() == CuratorFrameworkState.STARTED);

        separatedCluster = clusterList.stream().collect(Collectors.toSet());

        this.listener = (curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                LOGGER.info("Re-registering with ZK.");
                try {
                    deleteNodes();
                    createEphemeralNodes();
                } catch (Exception e) {
                    LOGGER.error("Exception recreating path", e);
                    throw new RuntimeException(e);
                }
            }
            if (connectionState == ConnectionState.LOST || connectionState == ConnectionState.SUSPENDED) {
                // todo: HealthCheckService.getInstance().setZkStatus(connectionState);
            }
        };

        framework.getConnectionStateListenable().addListener(listener);

        // Ensure the parent paths exist persistently
        createPersistNode(ZK_BASE_NODE);

        TreeCache cache = new TreeCache(framework, ZK_BASE_NODE);

        cache.getListenable().addListener(new ClusterStateListener());
        cache.start();

        // Ensure the parent paths exist persistently
        createPersistNode(ZK_CLUSTER_NODE);
        String clusterPath = ZK_CLUSTER_NODE + "/" + clusterService.getCurrentCluster();
        createPersistNode(clusterPath);

        TreeCache clusterCache = new TreeCache(framework, ZK_CLUSTER_NODE);
        clusterCache.getListenable().addListener(new ClusterStateListener());

        clusterCache.start();
    }

    private void createPersistNode(String name) throws Exception {
        while (framework.checkExists().forPath(name) == null) {
            LOGGER.info("Creating base node {}", name);
            framework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(name);
        }
    }

    public void register(String collectorIp, int port) throws Exception {
        String collectorAddress = collectorIp + ":" + port;

        String clusterPath = ZK_CLUSTER_NODE + "/" + clusterService.getCurrentCluster() + "/" + collectorAddress;
        String defaultPath = ZK_BASE_NODE + "/" + collectorAddress;
        if (separatedCluster.contains(clusterService.getCurrentCluster())) {
            // 如果是独立集群, 那么只重新注册独立集群的zk
            currentZkPath.add(clusterPath);
            deleteNode(clusterPath);
            createEphemeralNode(clusterPath);
        } else {
            // 如果是默认集群,那么不但注册独立集群的zk,还注册默认集群的zk
            currentZkPath.add(clusterPath);
            currentZkPath.add(defaultPath);
            deleteNode(clusterPath);
            createEphemeralNode(clusterPath);
            deleteNode(defaultPath);
            createEphemeralNode(defaultPath);
        }

        refreshAllClusterCollectors();
        refreshDefaultClusterCollectors();
    }

    @PreDestroy
    public void unregister() throws Exception {
        framework.getConnectionStateListenable().removeListener(listener);
        deleteNodes();
    }

    private void deleteNodes() throws Exception {
        for (String path : currentZkPath) {
            deleteNode(path);
        }
    }

    private void createEphemeralNodes() throws Exception {
        for (String path : currentZkPath) {
            createEphemeralNode(path);
        }
    }

    private void refreshDefaultClusterCollectors() throws Exception {
        List<String> collector = framework.getChildren().forPath(ZK_BASE_NODE);
        if (collector == null) {
            return;
        }
        for (String path : collector) {
            collectorAddressService.addDefaultClusterNode(path, parseData(path));
        }
    }

    private void refreshAllClusterCollectors() throws Exception {
        List<String> clusters = framework.getChildren().forPath(ZK_CLUSTER_NODE);
        if (clusters == null) {
            return;
        }
        for (String cluster : clusters) {
            List<String> collector = framework.getChildren().forPath(ZK_CLUSTER_NODE + "/" + cluster);
            if (collector == null) {
                continue;
            }
            for (String path : collector) {
                collectorAddressService.addForCluster(cluster, path, parseData(path));
            }
        }
    }

    private Collector parseData(String connection) {
        int l = connection.lastIndexOf(':');
        String host = connection.substring(0, l);
        String port = connection.substring(l + 1);
        return new Collector(host, Integer.valueOf(port));
    }

    private void createEphemeralNode(String path) throws Exception {
        framework.create()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);
    }

    private void deleteNode(String path) throws Exception {
        if (framework.checkExists().forPath(path) != null) {
            framework.delete().forPath(path);
        }
    }

    class ClusterStateListener implements TreeCacheListener {

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            if (event == null || event.getData() == null) {
                return;
            }
            String path = event.getData().getPath();
            String[] paths = null;
            String nodePath = null;
            if (path.startsWith(ZK_BASE_NODE) && !path.equals(ZK_BASE_NODE)) {
                nodePath = path.substring(ZK_BASE_NODE.length() + 1);
            } else if (path.startsWith(ZK_CLUSTER_NODE) && !path.equals(ZK_CLUSTER_NODE)) {
                nodePath = path.substring(ZK_CLUSTER_NODE.length() + 1);
                paths = nodePath.split("/");
            }

            switch (event.getType()) {
                case NODE_ADDED:
                    LOGGER.info("Collector node added: {},node path: {}.", path, nodePath);
                    if (paths != null && paths.length == 2) {
                        LOGGER.info("add new cluster [{}],collector:[{}]", paths[0], paths[1]);
                        collectorAddressService.addForCluster(paths[0], paths[1], parseData(paths[1]));
                    } else if (paths == null && nodePath != null) {
                        LOGGER.info("add default cluster collector:[{}]", nodePath);
                        collectorAddressService.addDefaultClusterNode(nodePath, parseData(nodePath));
                    }
                    break;
                case NODE_REMOVED:
                    LOGGER.info("Collector node removed: {},node path: {}.", path, nodePath);
                    if (paths != null && paths.length == 2) {
                        LOGGER.info("Remove cluster [{}],collector:[{}]", paths[0], paths[1]);
                        collectorAddressService.removeForCluster(paths[0], paths[1]);
                    } else if (paths == null && nodePath != null) {
                        LOGGER.info("remove default cluster collector:[{}]", nodePath);
                        collectorAddressService.removeDefaultClusterNode(nodePath);
                    }
                    break;
            }
        }
    }
}
