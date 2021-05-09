package io.etrace.api.repository;

import io.etrace.api.model.po.ui.DashboardAppPO;
import io.etrace.api.model.vo.ui.DashboardAppVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DashboardAppMapper extends PagingAndSortingRepository<DashboardAppPO, Long> {
    DashboardAppPO findByGlobalId(String globalId);

    int countByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(String title, String globalId, String status,
                                                                          String create, String update);

    List<DashboardAppPO> findByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(String title, String globalId,
                                                                                          String status, String create,
                                                                                          String update, Pageable page);

    int countByTitleAndCreatedByAndCritical(String title, String createdBy, Boolean critical);

    List<DashboardAppPO> findByTitleAndCreatedByAndCritical(String title, String createdBy, Boolean critical,
                                                            Pageable page);

    List<DashboardAppPO> findByTitleContainingAndIdIn(String title, List<Long> id);

    @Query("UPDATE dashboard_app   SET viewCount = (viewCount + 1)       WHERE id = ?1")
    @Modifying
    void updateUserFavorite(Long id);

    @Query("UPDATE dashboard_app   SET viewCount = (viewCount + 1)       WHERE id = ?1")
    @Modifying
    void updateUserView(Long id);

    @Query("UPDATE dashboard_app   SET favoriteCount = (favoriteCount - 1)      WHERE id = ?1")
    @Modifying
    void deleteUserFavorite(Long id);
}
