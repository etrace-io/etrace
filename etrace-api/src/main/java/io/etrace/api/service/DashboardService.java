package io.etrace.api.service;

import com.google.common.collect.Lists;
import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.Chart;
import io.etrace.api.model.po.ui.Dashboard;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.DashboardMapper;
import io.etrace.api.service.graph.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService extends BaseService<Dashboard> {

    private final DashboardMapper dashboardMapper;
    @Autowired
    private ChartService chartsService;

    @Autowired
    public DashboardService(DashboardMapper dashboardMapper) {
        super(dashboardMapper, UserActionService.dashboardCallback);
        this.dashboardMapper = dashboardMapper;
    }

    public List<Dashboard> findByIds(String title, List<Long> ids) {
        return dashboardMapper.findByTitleContainingAndIdIn(title, ids);
    }

    @Override
    public SearchResult<Dashboard> search(String title, String globalId, Integer pageNum, Integer pageSize, String user,
                                          String status) {
        SearchResult<Dashboard> result = new SearchResult<>();

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        Dashboard query = new Dashboard();
        query.setTitle(title);
        query.setGlobalId(globalId);
        query.setCreatedBy(user);
        query.setUpdatedBy(user);
        Example<Dashboard> exampleQuery = Example.of(query, matcher);
        Iterable<Dashboard> results = dashboardMapper.findAll(exampleQuery, PageRequest.of(pageNum - 1, pageSize));
        int count = dashboardMapper.count(exampleQuery);

        result.setTotal(count);
        result.setResults(Lists.newArrayList(results));
        return result;
    }

    @Override
    public Dashboard findById(long id, ETraceUser user) {
        Optional<Dashboard> op = findById(id);
        if (op.isPresent()) {
            Dashboard dashboard = op.get();
            List<Long> ids = dashboard.getChartIds();
            if (ids != null && !ids.isEmpty()) {
                List<Chart> charts = chartsService.findChartByIds(ids);
                dashboard.setCharts(charts);
            }
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (userAction != null) {
                List<Long> favorites = userAction.getFavoriteBoardIds();
                if (favorites != null && !favorites.isEmpty() && favorites.contains(dashboard.getId())) {
                    dashboard.setIsStar(true);
                }
            }
            return dashboard;
        } else {
            return null;
        }
    }

    public List<Dashboard> findByIds(String title, Long department, Long productLine, List<Long> dashboardIds) {
        return dashboardMapper.findByTitleContainingAndIdIn(title, dashboardIds);
    }

    public void updateDashboardChartIds(Dashboard dashboard, ETraceUser user) throws UserForbiddenException {
        createHistoryLog(findById(dashboard.getId()).orElse(null), user, HistoryLogTypeEnum.dashboard, true);
        dashboardMapper.save(dashboard);
    }

    @Override
    public void syncSonMetricConfig(Dashboard dashboard, ETraceUser user) {
        // todo: 这里怎么改成DashboardVO ?
        //dashboard.setChartIds(SyncUtil.syncCharts(dashboard.getCharts(), dashboard.getUpdatedBy(), chartsService));
    }

    @Override
    public void updateUserFavorite(long id) {
        dashboardMapper.updateUserFavorite(id);
    }

    @Override
    public void updateUserView(long id) {
        dashboardMapper.updateUserView(id);
    }

    @Override
    public void deleteUserFavorite(long id) {
        dashboardMapper.deleteUserFavorite(id);
    }

    @Override
    public Dashboard findByGlobalId(@NotNull String globalConfigId) {
        return dashboardMapper.findByGlobalId(globalConfigId);
    }
}
