package io.etrace.api.service;

import com.google.common.collect.Lists;
import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.Dashboard;
import io.etrace.api.model.po.ui.DashboardApp;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.DashboardAppMapper;
import io.etrace.api.service.graph.BaseService;
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

@Service
public class DashboardAppService extends BaseService<DashboardApp> {

    private final DashboardAppMapper dashboardAppMapper;
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    public DashboardAppService(DashboardAppMapper dashboardAppMapper) {
        super(dashboardAppMapper, UserActionService.dashboardAppCallback);
        this.dashboardAppMapper = dashboardAppMapper;
    }

    @Override
    public long create(DashboardApp dashboardApp) {
        dashboardAppMapper.save(dashboardApp);
        return dashboardApp.getId();
    }

    @Override
    public void update(DashboardApp dashboardApp, ETraceUser user) throws BadRequestException, UserForbiddenException {
        createHistoryLog(dashboardApp, user, HistoryLogTypeEnum.dashboardApp, true);
        dashboardAppMapper.save(dashboardApp);
    }

    @Override
    public List<DashboardApp> findByIds(String title, List<Long> ids) {
        return dashboardAppMapper.findByTitleContainingAndIdIn(title, ids);
    }

    @Override
    public DashboardApp findByGlobalId(@NotEmpty String globalConfigId) {
        return dashboardAppMapper.findByGlobalId(globalConfigId);
    }

    @Override
    public SearchResult<DashboardApp> search(String title, String globalId, Integer pageNum, Integer pageSize,
                                             String user,
                                             String status) {
        SearchResult<DashboardApp> result = new SearchResult<>();
        result.setTotal(dashboardAppMapper
            .countByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title, globalId, status, user, user));
        result.setResults(dashboardAppMapper.findByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title,
            globalId, status, user, user, PageRequest.of(pageNum - 1, pageSize)));
        return result;
    }

    @Override
    public Optional<DashboardApp> findById(long id) {
        Optional<DashboardApp> op = dashboardAppMapper.findById(id);
        if (op.isPresent()) {
            DashboardApp dashboardApp = op.get();
            if (dashboardApp.getDashboardIds() != null
                && dashboardApp.getDashboardIds().size() > 0) {
                List<Long> dashboardIds = dashboardApp.getDashboardIds();
                List<Dashboard> dashboards = Lists.newArrayList(dashboardService.findByIds(dashboardIds));
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
            return Optional.of(dashboardApp);
        }
        return Optional.empty();
    }

    @Override
    public DashboardApp findById(long id, ETraceUser user) {
        return findById(id).orElse(null);
    }

    @Override
    public void syncSonMetricConfig(DashboardApp dashboardApp, ETraceUser user) {

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

    public SearchResult<DashboardApp> search(String title, Long department, Long productLine, String user,
                                             Integer pageNum, Integer pageSize, Boolean critical) {
        SearchResult<DashboardApp> searchResult = new SearchResult<>();
        int count = dashboardAppMapper.countByTitleAndCreatedByAndCritical(title,
            user, critical);
        searchResult.setTotal(count);
        if (count > 0) {
            Integer start = (pageNum - 1) * pageSize;
            PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
            List<DashboardApp> dashboardApps = dashboardAppMapper
                .findByTitleAndCreatedByAndCritical(
                    title, user, critical, pageRequest);

            searchResult.setResults(dashboardApps);
        }
        return searchResult;
    }

    public List<DashboardApp> settingSearch(String title, Boolean critical) {
        return dashboardAppMapper.findByTitleAndCreatedByAndCritical(
            title, null, critical, Pageable.unpaged());
    }

    public void dashboardAppIsStar(ETraceUser user, List<DashboardApp> dashboardApps) {
        if (user == null) {
            return;
        }
        if (dashboardApps != null && !dashboardApps.isEmpty()) {
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (userAction != null) {
                List<Long> apps = userAction.getFavoriteApps();
                if (apps != null && !apps.isEmpty()) {
                    for (DashboardApp app : dashboardApps) {
                        if (apps.contains(app.getId())) {
                            app.setIsStar(true);
                        }
                    }
                }
            }
        }
    }
}
