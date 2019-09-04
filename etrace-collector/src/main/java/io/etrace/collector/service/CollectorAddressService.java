package io.etrace.collector.service;

import io.etrace.common.modal.Collector;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

public interface CollectorAddressService {


    default List<Collector> getCollectorsByAppId(@Nullable String host, @Nullable String appId) {
        // todo:  me/ele/arch/etrace/collector/server/http/rest/CollectorAddressResource.java:17 处的 alibabaProxyConfig
        // 应迁移到这里
        // todo: io/etrace/collector/rest/CollectorAddressResource.java:92 emptyCollectorListAppid() 也应迁移到这里
        return getCollectorsByAppId(appId);
    }

    List<Collector> getCollectorsByAppId(String appId);

    void addDefaultClusterNode(String connection, Collector collector);

    void addForCluster(String cluster, String connection, Collector collector);

    /**
     * @param connection ip:port
     */
    void removeDefaultClusterNode(String connection);

    void removeForCluster(String cluster, String connection);

    // 目前只用于内部查看,不对外部使用
    Collection<Collector> getAll();

    // 目前只用于内部查看,不对外部使用
    Object internalDefaultCollectors();

    // 目前只用于内部查看,不对外部使用
    Object internalClusterCollectors();

    List<Collector> getCollectorsForCluster(String cluster);

    List<Collector> getDefaultCollectors();
}
