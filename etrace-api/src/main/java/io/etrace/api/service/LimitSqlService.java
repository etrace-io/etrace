package io.etrace.api.service;

import io.etrace.api.model.po.misc.LimitSql;
import io.etrace.api.repository.LimitSqlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LimitSqlService {
    @Autowired
    private LimitSqlMapper limitSqlMapper;

    public LimitSql create(LimitSql limitSql) {
        return limitSqlMapper.save(limitSql);
    }

    public LimitSql update(LimitSql limitSql) {
        return limitSqlMapper.save(limitSql);
    }

    public void updateStatus(Long id, String status, String updateBy) {
        Optional<LimitSql> op = limitSqlMapper.findById(id);
        op.ifPresent(result -> {
            result.setStatus(status);
            result.setUpdatedBy(updateBy);
            limitSqlMapper.save(result);
        });
    }

    public Iterable<LimitSql> findAll() {
        return limitSqlMapper.findAll();
    }
}
