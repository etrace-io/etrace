package io.etrace.api.service;

import com.google.common.base.Strings;
import io.etrace.api.model.po.misc.Config;
import io.etrace.common.constant.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHolder.class);

    private Map<String, Map<String, List<Config>>> configMapCache = new HashMap<>();

    @Autowired
    private ConfigService configService;

    public List<Config> queryConfigByCache(String idc, String appName, String configKey) {
        Map<String, List<Config>> configMap = configMapCache.get(configKey);
        if (null != configMap) {
            List<Config> configs = configMap.get(idc);
            if (!CollectionUtils.isEmpty(configs)) {
                if (Strings.isNullOrEmpty(appName)) {
                    return configs;
                } else {
                    List<Config> result = new ArrayList<>();
                    configs.forEach(config -> {
                        if (appName.equals(config.getAppName())) {
                            result.add(config);
                        }
                    });
                    if (!CollectionUtils.isEmpty(result)) {
                        return result;
                    }
                }
            }
        }
        LOGGER.info("configkey:[{}],idc:[{}],appName:[{}] could not find in the cache", configKey, idc, appName);
        return configService.queryConfig(idc, appName, configKey);
    }

    @Scheduled(initialDelay = 1000, fixedRate = 1 * 60 * 1000)
    public void reloadCache() {
        Iterable<Config> configList = configService.findAll();
        Map<String, Map<String, List<Config>>> newConfigMapCache = new HashMap<>();
        configList.forEach(config -> {
            if (!Status.Active.name().equals(config.getStatus())) {
                return;
            }
            Map<String, List<Config>> configKeyMap = newConfigMapCache.get(config.getKey());
            if (null == configKeyMap) {
                configKeyMap = new HashMap<>();
                newConfigMapCache.put(config.getKey(), configKeyMap);
            }
            List<Config> idcConfigList = configKeyMap.get(config.getIdc());
            if (null == idcConfigList) {
                idcConfigList = new ArrayList<>();
                configKeyMap.put(config.getIdc(), idcConfigList);
            }
            idcConfigList.add(config);
        });
        if (newConfigMapCache.size() > 0) {
            configMapCache = newConfigMapCache;
        }
    }
}
