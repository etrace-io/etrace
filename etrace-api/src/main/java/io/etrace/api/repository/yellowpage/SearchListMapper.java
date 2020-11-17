package io.etrace.api.repository.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SearchListMapper extends PagingAndSortingRepository<SearchList, Long> {

    int countByNameAndStatus(String name, String status);

    List<SearchList> findAllByNameAndStatus(String name, String status, Pageable pageable);

    List<SearchList> findByListType(int listType);

    List<SearchList> findByIdIn(List<Long> idList);
}
