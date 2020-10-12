package io.etrace.api.service;

import com.google.common.collect.Maps;
import io.etrace.api.model.po.misc.ProxyConfig;
import io.etrace.api.repository.ProxyConfigMapper;
import io.etrace.common.constant.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 查询代理配置，分为两种，一种是需要多机房聚合，另一种不需要（但是需要做简单的负载均衡）
 */
@Service
public class ProxyConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigService.class);

    private Map<String/*proxyPath*/, ProxyConfig> configCache = new HashMap();

    @Autowired
    private ProxyConfigMapper proxyConfigMapper;

    public long create(ProxyConfig proxyConfig) {
        proxyConfigMapper.save(proxyConfig);
        return proxyConfig.getId();
    }

    public void update(ProxyConfig proxyConfig) {
        proxyConfigMapper.save(proxyConfig);
    }

    public Iterable<ProxyConfig> findAll() {
        return proxyConfigMapper.findAll();
    }

    public Map<String, String> getConsoleUrl(String clusters) {
        //TODO
        //        Map<String, String> servers = new HashMap<>();
        //        String[] clusterArr = clusters.split(",");
        //        for (String cluster : clusterArr) {
        //
        //        }
        //        return servers;
        return Maps.newHashMap();
    }

    public ProxyConfig getConfigByUniqueKey(String proxyPath) {
        ProxyConfig proxyConfig = configCache.get(proxyPath);
        if (null == proxyConfig) {
            LOGGER.info("proxyPath :[{}] could not find in the cache!", proxyPath);
            return proxyConfigMapper.findByProxyPath(proxyPath);
        } else {
            return proxyConfig;
        }
    }

    @Scheduled(initialDelay = 1000, fixedRate = 5 * 60 * 1000)
    public void reloadCache() {
        Iterable<ProxyConfig> proxyConfigList = proxyConfigMapper.findAll();
        Map<String, ProxyConfig> newConfigCache = new HashMap<>();
        proxyConfigList.forEach(proxyConfig -> {
            if (Status.Active.name().equals(proxyConfig.getStatus())) {
                newConfigCache.put(proxyConfig.getProxyPath(), proxyConfig);
            }
        });
        if (newConfigCache.size() > 0) {
            configCache = newConfigCache;
        }

    }

    public void updateStatus(Long id, String status) {
        Optional<ProxyConfig> op = proxyConfigMapper.findById(id);
        op.ifPresent(result -> {
            result.setStatus(status);
            proxyConfigMapper.save(result);
        });
    }
}
