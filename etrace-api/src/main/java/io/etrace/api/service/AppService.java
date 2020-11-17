package io.etrace.api.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.etrace.api.model.po.ui.App;
import io.etrace.api.repository.AppMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppService.class);

    private final Map<String, App> appCache = new HashMap<>();
    private final Map<String, App> mysqlAppCache = new HashMap<>();
    @Autowired
    private AppMapper appMapper;

    public long create(App app) {
        return appMapper.save(app).getId();
    }

    public void update(App app) {
        appMapper.save(app);
    }

    /**
     * 不包括 部门信息，只有部门id
     */
    public List<App> findByOwner(String owner) {
        return appMapper.findByOwner(owner);
    }

    /**
     * 不包括 部门信息，只有部门id
     */
    public App findByAppId(String appId) {
        return appMapper.findByAppId(appId);
    }

    public List<App> findLikeAppId(String appId, Boolean critical) {
        return appMapper.findByAppIdAndCritical(appId, critical);
    }

    public Set<String> findAppIdFromMysqlAndPgCache(String appIdKey) {
        Map<String, App> appIdFromMysqlCache = findAppIdFromMysqlCache(appIdKey);
        Map<String, App> appIdByAppIdKey = findAppIdByAppIdKey(appIdKey);
        Set<String> result = new HashSet<>(appIdFromMysqlCache.size() + appIdByAppIdKey.size());
        result.addAll(appIdByAppIdKey.keySet());
        result.addAll(appIdFromMysqlCache.keySet());
        return result;
    }

    public List<App> findAppFromPgCache(String appIdKey) {
        Map<String, App> appIdByAppIdKey = findAppIdByAppIdKey(appIdKey);
        List<App> result = new ArrayList<>(appIdByAppIdKey.size());
        result.addAll(appIdByAppIdKey.values());
        return result;
    }

    private Map<String, App> findAppIdFromMysqlCache(String appIdKey) {
        Map<String, App> map = new HashMap<>();
        if (appIdKey.contains(" ")) {
            List<String> keys = Splitter.on(" ").trimResults().splitToList(appIdKey);
            if (keys.size() > 5) { // at most 5 keys
                keys = keys.subList(0, 5);
            }
            Set<String> keysSet = Sets.newHashSet(keys);
            OUT:
            for (Map.Entry<String, App> entry : mysqlAppCache.entrySet()) {
                for (String key : keysSet) {
                    if (!entry.getKey().contains(key)) {
                        continue OUT;
                    }
                }
                map.put(entry.getKey(), entry.getValue());
            }
        } else {
            mysqlAppCache.forEach((key, value) -> {
                if (key.contains(appIdKey)) {
                    map.put(key, value);
                }
            });
        }
        return map;
    }

    private Map<String, App> findAppIdByAppIdKey(String appIdKey) {

        Map<String, App> map = new HashMap<>();
        if (appIdKey.contains(" ")) {
            List<String> keys = Splitter.on(" ").trimResults().splitToList(appIdKey);
            if (keys.size() > 5) { // at most 5 keys
                keys = keys.subList(0, 5);
            }

            Set<String> keysSet = Sets.newHashSet(keys);
            OUT:
            for (Map.Entry<String, App> entry : appCache.entrySet()) {
                for (String key : keysSet) {
                    if (!entry.getKey().contains(key)) {
                        continue OUT;
                    }
                }
                map.put(entry.getKey(), entry.getValue());
            }
        } else {
            appCache.forEach((key, value) -> {
                if (key.contains(appIdKey)) {
                    map.put(key, value);
                }
            });
        }
        return map;
    }
}
