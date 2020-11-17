package io.etrace.api.repository.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SearchRecordMapper extends PagingAndSortingRepository<SearchRecord, Long> {

    @Modifying
    @Query("UPDATE search_record SET favoriteIndex = (favoriteIndex + ?2)WHERE id = ?1")
    void updateUserFavorite(Long id, int num);

    @Modifying
    @Query("UPDATE search_record SET clickIndex = (clickIndex + ?2)WHERE id = ?1")
    void updateUserView(Long id, int num);

    int countByIdAndNameAndStatus(Long listId, String name, String status);

    List<SearchRecord> findAllByIdInAndStatus(List<Long> idList, String status, Pageable page);

    List<SearchRecord> findAllByOrderByFavoriteIndex(Pageable page);

    List<SearchRecord> findAllByOrderByClickIndex(Pageable page);

    List<SearchRecord> findByUrl(String url);
}
