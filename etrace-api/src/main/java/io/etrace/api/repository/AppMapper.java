package io.etrace.api.repository;

import io.etrace.api.model.po.ui.App;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AppMapper extends CrudRepository<App, Long> {

    App findByAppId(String appId);

    List<App> findByOwner(String owner);

    List<App> findByAppIdAndCritical(String appId, Boolean critical);
}
