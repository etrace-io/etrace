package io.etrace.api.service;

import com.google.common.collect.Streams;
import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.DashboardPO;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.ChartVO;
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.repository.DashboardMapper;
import io.etrace.api.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService extends BaseService<DashboardVO, DashboardPO> {

    private final DashboardMapper dashboardMapper;
    @Autowired
    private ChartService chartsService;

    @Autowired
    public DashboardService(DashboardMapper dashboardMapper) {
        super(dashboardMapper, UserActionService.dashboardCallback);
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public List<DashboardVO> findByIds(String title, List<Long> ids) {
        return dashboardMapper.findByTitleContainingAndIdIn(title, ids)
            .stream().map(DashboardVO::toVO).collect(Collectors.toList());
    }

    @Override
    public SearchResult<DashboardVO> search(String title, String globalId, Integer pageNum, Integer pageSize,
                                            String user,
                                            String status) {
        SearchResult<DashboardVO> result = new SearchResult<>();

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        DashboardPO query = new DashboardPO();
        query.setTitle(title);
        query.setGlobalId(globalId);
        query.setCreatedBy(user);
        query.setUpdatedBy(user);
        Example<DashboardPO> exampleQuery = Example.of(query, matcher);
        Iterable<DashboardPO> results = dashboardMapper.findAll(exampleQuery, PageRequest.of(pageNum - 1, pageSize));
        int count = dashboardMapper.count(exampleQuery);

        result.setTotal(count);
        result.setResults(Streams.stream(results).map(DashboardVO::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public DashboardPO findById(long id, ETraceUser user) {
        Optional<DashboardPO> op = findById(id);
        return op.orElse(null);
        // todo: 补回 dashboard.setCharts(charts); 的拼装逻辑
        //if (op.isPresent()) {
        //    DashboardVO dashboard = DashboardVO.toVO(op.get());
        //    List<Long> ids = dashboard.getChartIds();
        //    if (ids != null && !ids.isEmpty()) {
        //        List<ChartVO> charts = chartsService.findChartByIds(ids);
        //        dashboard.setCharts(charts);
        //    }
        //    UserAction userAction = userActionService.findFavoriteByUser(user);
        //    if (userAction != null) {
        //        List<Long> favorites = userAction.getFavoriteBoardIds();
        //        if (favorites != null && !favorites.isEmpty() && favorites.contains(dashboard.getId())) {
        //            dashboard.setIsStar(true);
        //        }
        //    }
        //    return dashboard;
        //} else {
        //    return null;
        //}
    }

    @Override
    public <S extends DashboardPO> void syncSonMetricConfig(S t, ETraceUser user) {
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
    public void syncMetricConfig(DashboardVO dashboardVO, ETraceUser user) throws Exception {

    }

    @Override
    public DashboardVO findByGlobalId(@NotNull String globalConfigId) {
        return DashboardVO.toVO(dashboardMapper.findByGlobalId(globalConfigId));
    }

    public List<DashboardVO> findByIds(String title, Long department, Long productLine, List<Long> dashboardIds) {
        return dashboardMapper.findByTitleContainingAndIdIn(title, dashboardIds)
            .stream().map(DashboardVO::toVO).collect(Collectors.toList());
    }

    public void updateDashboardChartIds(DashboardVO dashboard, ETraceUser user) throws UserForbiddenException {
        createHistoryLog(findById(dashboard.getId()).orElse(null), user, HistoryLogTypeEnum.dashboard, true);
        dashboardMapper.save(dashboard.toPO());
    }
}
