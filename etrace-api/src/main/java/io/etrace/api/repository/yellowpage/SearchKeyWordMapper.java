package io.etrace.api.repository.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchKeyWord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SearchKeyWordMapper extends PagingAndSortingRepository<SearchKeyWord, Long> {
    int countByStatusAndNameContaining(String status, String name);

    List<SearchKeyWord> findByStatusAndNameContaining(String status, String name, Pageable page);

    List<SearchKeyWord> findByIdIn(List<Long> id);
}
