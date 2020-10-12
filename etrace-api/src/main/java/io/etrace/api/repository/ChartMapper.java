package io.etrace.api.repository;

import io.etrace.api.model.po.ui.Chart;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChartMapper extends CrudRepository<Chart, Long> {

    int countByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(String title, String globalId, String createdBy,
                                                                    String status, boolean isAdmin);

    List<Chart> findByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(String title, String globalId,
                                                                           String createdBy, String status,
                                                                           boolean isAdmin, Pageable page);

    List<Chart> findByIdIn(List<Long> ids);

    Chart findByGlobalId(String globalId);
}
