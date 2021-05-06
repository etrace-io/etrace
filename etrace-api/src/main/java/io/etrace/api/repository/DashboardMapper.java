package io.etrace.api.repository;

import io.etrace.api.model.vo.ui.Dashboard;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DashboardMapper extends PagingAndSortingRepository<Dashboard, Long> {

    Dashboard findByGlobalId(String globalId);

    int countByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(String title, String globalId, String status,
                                                                          String create, String update);

    List<Dashboard> findByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(String title, String globalId,
                                                                                     String status, String create,
                                                                                     String update, Pageable page);

    List<Dashboard> findByTitleContainingAndIdIn(String title, List<Long> id);

    @Query("UPDATE dashboard   SET viewCount = (viewCount + 1) WHERE id = ?1")
    @Modifying
    void updateUserFavorite(Long id);

    @Query("UPDATE dashboard   SET viewCount = (viewCount + 1) WHERE id = ?1")
    @Modifying
    void updateUserView(Long id);

    @Query("UPDATE dashboard   SET favoriteCount = (favoriteCount - 1) WHERE id = ?1")
    @Modifying
    void deleteUserFavorite(Long id);

    Iterable<Dashboard> findAll(Example<Dashboard> example, Pageable page);

    int count(Example<Dashboard> example);
}
