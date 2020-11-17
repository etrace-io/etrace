package io.etrace.api.repository;

import io.etrace.api.model.po.ui.ApplyTokenLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ApplyTokenLogMapper extends PagingAndSortingRepository<ApplyTokenLog, Long> {

    int countByAuditStatusAndStatusAndUserEmail(String auditStatus, String status, String userCode);

    List<ApplyTokenLog> findByAuditStatusAndStatusAndUserEmail(String auditStatus, String status, String userCode,
                                                               Pageable pageable);
}
