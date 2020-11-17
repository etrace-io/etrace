package io.etrace.api.repository;

import io.etrace.api.model.po.ui.MetricDataSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DataSourceMapper extends PagingAndSortingRepository<MetricDataSource, Long> {

    List<MetricDataSource> findAllByTypeAndNameAndStatus(String type, String name, String status, Pageable page);

    int countByTypeAndNameAndStatus(String type, String name, String status);

    @Query("UPDATE datasource d SET d.status = ?2 WHERE d.id = ?1")
    @Modifying
    void updateStatus(long id, String status);

}
