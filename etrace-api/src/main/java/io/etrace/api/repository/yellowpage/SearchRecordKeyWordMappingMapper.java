package io.etrace.api.repository.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchRecordKeyWordMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SearchRecordKeyWordMappingMapper extends PagingAndSortingRepository<SearchRecordKeyWordMapping, Long> {

    @Modifying
    @Query(" UPDATE search_record_keyword_mapping        SET status         =  ?2        WHERE id in ?1")
    void batchUpdateStatus(List<Long> idList, String status);

    List<SearchRecordKeyWordMapping> findByRecordIdAndKeywordIdInAndStatus(Long recordId, List<Long> keywordId,
                                                                           String status, Pageable page);
}
