package io.etrace.api.repository;

import io.etrace.api.model.po.ui.ChartPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChartMapper extends CrudRepository<ChartPO, Long> {

    int countByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(String title, String globalId, String createdBy,
                                                                    String status, boolean isAdmin);

    List<ChartPO> findByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(String title, String globalId,
                                                                             String createdBy, String status,
                                                                             boolean isAdmin, Pageable page);

    List<ChartPO> findByIdIn(List<Long> ids);

    ChartPO findByGlobalId(String globalId);
}
