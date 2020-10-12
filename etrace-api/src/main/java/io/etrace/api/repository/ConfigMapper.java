package io.etrace.api.repository;

import io.etrace.api.model.po.misc.Config;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConfigMapper extends CrudRepository<Config, Long> {
    List<Config> findByKey(String key);

    List<Config> findByIdcAndAppNameAndKey(String idc, String appName, String key);
}
