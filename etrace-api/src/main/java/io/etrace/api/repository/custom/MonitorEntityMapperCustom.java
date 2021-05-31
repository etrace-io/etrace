package io.etrace.api.repository.custom;//package io.etrace.api.repository.custom;

import io.etrace.api.model.po.ui.MonitorEntity;

import java.util.List;
import java.util.Optional;

public interface MonitorEntityMapperCustom {

    List<MonitorEntity> findAllByParentId(long parentId);

    List<MonitorEntity> findByTypeAndStatus(String type, String status);

    Optional<MonitorEntity> findByCode(String code);
}
