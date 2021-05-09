package io.etrace.api.service;

import com.google.common.collect.Lists;
import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.DashboardAppPO;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.DashboardAppVO;
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.repository.DashboardAppMapper;
import io.etrace.api.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardAppService extends BaseService<DashboardAppVO, DashboardAppPO> {

    private final DashboardAppMapper dashboardAppMapper;
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    public DashboardAppService(DashboardAppMapper dashboardAppMapper) {
        super(dashboardAppMapper, UserActionService.dashboardAppCallback);
        this.dashboardAppMapper = dashboardAppMapper;
    }

    public long create(DashboardAppVO dashboardApp) {
        dashboardAppMapper.save(dashboardApp.toPO());
        return dashboardApp.getId();
    }

    public void update(DashboardAppVO dashboardApp, ETraceUser user)
        throws BadRequestException, UserForbiddenException {
        createHistoryLog(dashboardApp.toPO(), user, HistoryLogTypeEnum.dashboardApp, true);
        dashboardAppMapper.save(dashboardApp.toPO());
    }

    public List<DashboardAppVO> findByIds(String title, List<Long> ids) {
        return dashboardAppMapper.findByTitleContainingAndIdIn(title, ids).stream().map(DashboardAppVO::toVO).collect(
            Collectors.toList());
    }

    @Override
    public void syncMetricConfig(DashboardAppVO dashboardAppVO, ETraceUser user) throws Exception {

    }

    @Override
    public DashboardAppVO findByGlobalId(@NotEmpty String globalConfigId) {
        return DashboardAppVO.toVO(dashboardAppMapper.findByGlobalId(globalConfigId));
    }

    @Override
    public SearchResult<DashboardAppVO> search(String title, String globalId, Integer pageNum, Integer pageSize,
                                               String user,
                                               String status) {
        SearchResult<DashboardAppVO> result = new SearchResult<>();
        result.setTotal(dashboardAppMapper
            .countByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title, globalId, status, user, user));
        result.setResults(
            dashboardAppMapper.findByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title,
                globalId, status, user, user, PageRequest.of(pageNum - 1, pageSize))
                .stream().map(DashboardAppVO::toVO).collect(Collectors.toList())
        );
        return result;
    }

    @Override
    public Optional<DashboardAppPO> findById(long id) {
        Optional<DashboardAppPO> op = dashboardAppMapper.findById(id);
        if (op.isPresent()) {
            DashboardAppVO dashboardApp = DashboardAppVO.toVO(op.get());
            if (dashboardApp.getDashboardIds() != null
                && dashboardApp.getDashboardIds().size() > 0) {
                List<Long> dashboardIds = dashboardApp.getDashboardIds();
                List<DashboardVO> dashboards = Lists.newArrayList(dashboardService.findByIds(dashboardIds));
                // sort the dashboard
                if (!CollectionUtils.isEmpty(dashboards)) {
                    LinkedHashMap<Long, Integer> dashboardIdMap = new LinkedHashMap<>();
                    for (int i = 0; i < dashboardIds.size(); i++) {
                        dashboardIdMap.put(dashboardIds.get(i), i);
                    }
                    dashboards.sort(Comparator.comparingInt(o -> dashboardIdMap.get(o.getId())));
                }
                dashboardApp.setDashboards(dashboards);
            }
            return Optional.of(dashboardApp.toPO());
        }
        return Optional.empty();
    }

    @Override
    public DashboardAppPO findById(long id, ETraceUser user) {
        return findById(id).orElseGet(null);
    }

    @Override
    public <S extends DashboardAppPO> void syncSonMetricConfig(S t, ETraceUser user) {

    }

    @Override
    public void updateUserFavorite(long id) {
        dashboardAppMapper.updateUserFavorite(id);
    }

    @Override
    public void updateUserView(long id) {
        dashboardAppMapper.updateUserView(id);
    }

    @Override
    public void deleteUserFavorite(long id) {
        dashboardAppMapper.deleteUserFavorite(id);
    }

    public SearchResult<DashboardAppVO> search(String title, Long department, Long productLine, String user,
                                               Integer pageNum, Integer pageSize, Boolean critical) {
        SearchResult<DashboardAppVO> searchResult = new SearchResult<>();
        int count = dashboardAppMapper.countByTitleAndCreatedByAndCritical(title,
            user, critical);
        searchResult.setTotal(count);
        if (count > 0) {
            Integer start = (pageNum - 1) * pageSize;
            PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
            List<DashboardAppVO> dashboardApps = dashboardAppMapper
                .findByTitleAndCreatedByAndCritical(title, user, critical, pageRequest)
                .stream().map(DashboardAppVO::toVO).collect(Collectors.toList());

            searchResult.setResults(dashboardApps);
        }
        return searchResult;
    }

    public List<DashboardAppVO> settingSearch(String title, Boolean critical) {
        return dashboardAppMapper.findByTitleAndCreatedByAndCritical(
            title, null, critical, Pageable.unpaged()).stream().map(DashboardAppVO::toVO).collect(Collectors.toList());
    }

    public void dashboardAppIsStar(ETraceUser user, List<DashboardAppVO> dashboardApps) {
        if (user == null) {
            return;
        }
        if (dashboardApps != null && !dashboardApps.isEmpty()) {
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (userAction != null) {
                List<Long> apps = userAction.getFavoriteApps();
                if (apps != null && !apps.isEmpty()) {
                    for (DashboardAppVO app : dashboardApps) {
                        if (apps.contains(app.getId())) {
                            app.setIsStar(true);
                        }
                    }
                }
            }
        }
    }
}
