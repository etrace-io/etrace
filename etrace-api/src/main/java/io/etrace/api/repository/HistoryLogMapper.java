package io.etrace.api.repository;

import io.etrace.api.model.po.ui.HistoryLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HistoryLogMapper extends CrudRepository<HistoryLog, Long> {

    List<HistoryLog> findByHistoryIdAndType(Long historyId, String type, Pageable page);

    int countByHistoryIdAndType(Long historyId, String type);
}
