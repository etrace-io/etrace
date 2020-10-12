package io.etrace.api.repository.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchListRecordMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SearchListRecordMappingMapper extends PagingAndSortingRepository<SearchListRecordMapping, Long> {

    @Modifying
    @Query(" UPDATE search_list_record_mapping        SET status    =  ?2       WHERE id in ?1")
    void batchUpdateStatus(List<Long> idList, String status);

    List<SearchListRecordMapping> findByListIdAndRecordIdInAndStatus(Long listId, List<Long> recordIdList,
                                                                     String status, Pageable page);
}
