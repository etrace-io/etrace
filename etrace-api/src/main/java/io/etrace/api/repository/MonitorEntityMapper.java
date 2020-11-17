package io.etrace.api.repository;

import io.etrace.api.model.po.ui.MonitorEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MonitorEntityMapper extends CrudRepository<MonitorEntity, Long> {

    List<MonitorEntity> findAllByParentId(long parentId);

    List<MonitorEntity> findByTypeAndStatus(String type, String status);

    Optional<MonitorEntity> findByCode(String code);
}
