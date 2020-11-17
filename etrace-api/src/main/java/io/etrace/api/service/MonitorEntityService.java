package io.etrace.api.service;

import io.etrace.api.model.po.ui.MonitorEntity;
import io.etrace.api.repository.MonitorEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class MonitorEntityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorEntityService.class);

    @Autowired
    private MonitorEntityMapper monitorEntityMapper;

    public Long create(MonitorEntity entity) {
        monitorEntityMapper.save(entity);
        return entity.getId();

    }

    public List<MonitorEntity> findByParentId(long parentId, String status) {
        List<MonitorEntity> monitorEntityList = monitorEntityMapper.findAllByParentId(parentId);
        if (!StringUtils.isEmpty(status)) {
            checkStatus(monitorEntityList, status);
        }
        return monitorEntityList;
    }

    public Optional<MonitorEntity> findByCode(String code) {
        return monitorEntityMapper.findByCode(code);
    }

    private void checkStatus(List<MonitorEntity> list, String status) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Iterator<MonitorEntity> iterator = list.iterator();
        while (iterator.hasNext()) {
            MonitorEntity monitorEntity = iterator.next();
            if (!status.equals(monitorEntity.getStatus())) {
                iterator.remove();
            } else {
                checkStatus(monitorEntity.getChildren(), status);
            }
        }
    }

    public List<MonitorEntity> findEntityByType(String type, String status) {
        return monitorEntityMapper.findByTypeAndStatus(type, status);
    }

    public void update(MonitorEntity entity) {
        monitorEntityMapper.save(entity);
    }

    public Iterable<MonitorEntity> findAll() {
        return monitorEntityMapper.findAll();
    }

    public void changeStatus(MonitorEntity monitorEntity) {
        monitorEntityMapper.save(monitorEntity);
    }
}
