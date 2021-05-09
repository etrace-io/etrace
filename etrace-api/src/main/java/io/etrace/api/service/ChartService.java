package io.etrace.api.service;

import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.ChartPO;
import io.etrace.api.model.po.ui.HistoryLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.ChartVO;
import io.etrace.api.repository.ChartMapper;
import io.etrace.api.service.base.SyncMetricConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChartService implements SyncMetricConfigService<ChartVO> {
    @Autowired
    private ChartMapper chartMapper;
    @Autowired
    private HistoryLogService historyLogService;

    public SearchResult<ChartVO> search(String title, String globalId, Long department, Long productLine, String user,
                                        int pageNum, int pageSize, String status) {
        SearchResult<ChartVO> searchResult = new SearchResult<ChartVO>();
        // todo 删除
        //        int count = chartMapper
        //            .countByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(title, globalId, user, status, true);
        //        searchResult.setTotal(count);
        //        if (count > 0) {
        //            Integer start = (pageNum - 1) * pageSize;
        //            PageRequest page = PageRequest.of(pageNum - 1, pageSize);
        //            List<Chart> charts = chartMapper
        //                .findByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(title, globalId, user, status,
        //                true, page);
        //            searchResult.setResults(charts);
        //        }
        return searchResult;
    }

    public ChartVO findChartById(Long id) {
        Optional<ChartPO> op = chartMapper.findById(id);
        return op.map(ChartVO::toVO).orElse(null);
    }

    public List<ChartVO> findChartByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return chartMapper.findByIdIn(ids).stream().map(ChartVO::toVO).collect(Collectors.toList());
    }

    public Long create(ChartVO chart) throws BadRequestException {
        if (null == chart.getAdminVisible()) {
            chart.setAdminVisible(Boolean.FALSE);
        }
        if (StringUtils.isEmpty(chart.getGlobalId())) {
            throw new BadRequestException("global Id must not be null");
        }
        ChartVO globalIdChart = findByGlobalId(chart.getGlobalId());
        if (null != globalIdChart) {
            throw new BadRequestException("the global id must be unique！");
        }
        chartMapper.save(chart.toPO());
        return chart.getId();
    }

    public void update(ChartVO chart, ETraceUser user) throws UserForbiddenException, BadRequestException {
        chart.setUpdatedBy(user.getUsername());
        ChartVO globalIdChart = ChartVO.toVO(chartMapper.findByGlobalId(chart.getGlobalId()));
        if (StringUtils.isEmpty(chart.getGlobalId())) {
            throw new BadRequestException("global Id could not be set empty!");
        }
        if (null != globalIdChart && !globalIdChart.getId().equals(chart.getId())) {
            throw new BadRequestException("could not modify duplicate global id");
        }

        //if the dashboard is set to be editable by the administrator, verify that the current user have a role of admin
        if (Boolean.TRUE.equals(chart.getAdminVisible()) && !user.isAdmin()) {
            throw new UserForbiddenException("no permission,the chart is set to the administrator to update！");
        }

        createHistoryLog(chart.getId(), chart.getUpdatedBy());
        chartMapper.save(chart.toPO());
    }

    public void changeChartStatus(ChartVO chart) {
        createHistoryLog(chart.getId(), chart.getUpdatedBy());
        chartMapper.save(chart.toPO());
    }

    @Override
    public void syncMetricConfig(ChartVO chart, ETraceUser user) throws UserForbiddenException {
        if (null == chart) {
            throw new RuntimeException("chart is null");
        }
        chart.setUpdatedBy(user.getUsername());
        chart.setCreatedBy(user.getUsername());
        if (StringUtils.isEmpty(chart.getGlobalId())) {
            throw new RuntimeException("global id is null");
        }
        ChartVO oldChart = findByGlobalId(chart.getGlobalId());
        if (null != oldChart) {
            //update chart info in addtition to created_at，created_by，the primary key id
            chart.setId(oldChart.getId());
            //if the dashboard is set to be editable by the administrator, verify that the current user have a role
            // of admin
            if (Boolean.TRUE.equals(chart.getAdminVisible()) && !user.isAdmin()) {
                throw new UserForbiddenException("no permission,the chart is set to the administrator to update！");
            }
            createHistoryLog(oldChart, chart.getUpdatedBy());
            chartMapper.save(chart.toPO());
        } else {
            if (null == chart.getAdminVisible()) {
                chart.setAdminVisible(Boolean.FALSE);
            }
            chartMapper.save(chart.toPO());
        }

    }

    @Override
    public ChartVO findByGlobalId(String globalConfigId) {
        if (StringUtils.isEmpty(globalConfigId)) {
            return null;
        }
        return ChartVO.toVO(chartMapper.findByGlobalId(globalConfigId));
    }

    /**
     * create chart history before modify the chart
     *
     * @param id   chart id
     * @param user current user
     */
    private void createHistoryLog(Long id, String user) {
        Optional<ChartPO> op = chartMapper.findById(id);
        op.ifPresent(chartPO -> createHistoryLog(ChartVO.toVO(chartPO), user));
    }

    /**
     * create chart history before modify the chart
     *
     * @param chart    the oldVersion chart
     * @param userName the current user
     * @throws UserForbiddenException
     */
    private void createHistoryLog(ChartVO chart, String userName) {
        HistoryLog historyLog = new HistoryLog();
        historyLog.setHistory(chart);
        historyLog.setCreatedBy(userName);
        historyLog.setUpdatedBy(userName);
        historyLog.setType(HistoryLogTypeEnum.chart.name());
        historyLog.setHistoryId(chart.getId());
        historyLogService.create(historyLog);

    }
}
