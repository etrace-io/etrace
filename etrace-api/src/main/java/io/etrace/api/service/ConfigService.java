package io.etrace.api.service;

import io.etrace.api.model.po.misc.Config;
import io.etrace.api.repository.ConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ConfigMapper configMapper;

    public Long create(Config config) {
        configMapper.save(config);
        return config.getId();
    }

    public void update(Config config) {
        configMapper.save(config);
    }

    public List<Config> findByKey(String key) {
        return configMapper.findByKey(key);
    }

    public void deleteConfig(Long id, String status) {
        Config config = new Config();
        config.setId(id);
        config.setStatus(status);
        configMapper.save(config);
    }

    public Iterable<Config> findAll() {
        return configMapper.findAll();
    }

    public List<Config> queryConfig(String idc, String appName, String configKey) {
        return configMapper.findByIdcAndAppNameAndKey(idc, appName, configKey);
    }

}
