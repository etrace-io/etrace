package io.etrace.api.service.base;

import io.etrace.api.model.po.user.ETraceUser;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * sync metric config (chart,dashboard,dashboardApp) to another environment
 */
public interface SyncMetricConfigService<T> {

    /**
     * sync config
     */
    void syncMetricConfig(@NotNull T t, ETraceUser user) throws Exception;

    /**
     * check the global is duplicate，need many environment
     *
     * @param globalConfigId the Unique global id in different env
     */
    T findByGlobalId(@NotEmpty String globalConfigId);

}
