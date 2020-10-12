package io.etrace.api.service;

import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.Chart;
import io.etrace.api.model.po.ui.HistoryLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.ChartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChartService implements SyncMetricConfigService<Chart> {
    @Autowired
    private ChartMapper chartMapper;
    @Autowired
    private HistoryLogService historyLogService;

    public SearchResult<Chart> search(String title, String globalId, Long department, Long productLine, String user,
                                      int pageNum, int pageSize, String status) {
        SearchResult<Chart> searchResult = new SearchResult<Chart>();
        int count = chartMapper
            .countByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(title, globalId, user, status, true);
        searchResult.setTotal(count);
        if (count > 0) {
            Integer start = (pageNum - 1) * pageSize;
            PageRequest page = PageRequest.of(pageNum - 1, pageSize);
            List<Chart> charts = chartMapper
                .findByTitleAndGlobalIdAndCreatedByAndStatusAndAdminVisible(title, globalId, user, status, true, page);
            searchResult.setResults(charts);
        }
        return searchResult;
    }

    public Optional<Chart> findChartById(Long id) {
        return chartMapper.findById(id);
    }

    public List<Chart> findChartByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return chartMapper.findByIdIn(ids);
    }

    public Long create(Chart chart) throws BadRequestException {
        if (null == chart.getAdminVisible()) {
            chart.setAdminVisible(Boolean.FALSE);
        }
        if (StringUtils.isEmpty(chart.getGlobalId())) {
            throw new BadRequestException("global Id must not be null");
        }
        Chart globalIdChart = findByGlobalId(chart.getGlobalId());
        if (null != globalIdChart) {
            throw new BadRequestException("the global id must be unique！");
        }
        chartMapper.save(chart);
        return chart.getId();
    }

    public void update(Chart chart, ETraceUser user) throws UserForbiddenException, BadRequestException {
        chart.setUpdatedBy(user.getUsername());
        Chart globalIdChart = chartMapper.findByGlobalId(chart.getGlobalId());
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

        chartMapper.save(chart);

    }

    public void changeChartStatus(Chart chart) {
        createHistoryLog(chart.getId(), chart.getUpdatedBy());
        chartMapper.save(chart);
    }

    @Override
    public void syncMetricConfig(Chart chart, ETraceUser user) throws UserForbiddenException {
        if (null == chart) {
            throw new RuntimeException("chart is null");
        }
        chart.setUpdatedBy(user.getUsername());
        chart.setCreatedBy(user.getUsername());
        if (StringUtils.isEmpty(chart.getGlobalId())) {
            throw new RuntimeException("global id is null");
        }
        Chart oldChart = findByGlobalId(chart.getGlobalId());
        if (null != oldChart) {
            //update chart info in addtition to created_at，created_by，the primary key id
            chart.setId(oldChart.getId());
            //if the dashboard is set to be editable by the administrator, verify that the current user have a role
            // of admin
            if (Boolean.TRUE.equals(chart.getAdminVisible()) && !user.isAdmin()) {
                throw new UserForbiddenException("no permission,the chart is set to the administrator to update！");
            }
            createHistoryLog(oldChart, chart.getUpdatedBy());
            chartMapper.save(chart);
        } else {
            if (null == chart.getAdminVisible()) {
                chart.setAdminVisible(Boolean.FALSE);
            }
            chartMapper.save(chart);
        }

    }

    @Override
    public Chart findByGlobalId(String globalConfigId) {
        if (StringUtils.isEmpty(globalConfigId)) {
            return null;
        }
        return chartMapper.findByGlobalId(globalConfigId);
    }

    /**
     * create chart history before modify the chart
     *
     * @param id   chart id
     * @param user current user
     */
    private void createHistoryLog(Long id, String user) {
        Optional<Chart> op = chartMapper.findById(id);
        if (op.isPresent()) {
            createHistoryLog(op.get(), user);
        }
    }

    /**
     * create chart history before modify the chart
     *
     * @param chart    the oldVersion chart
     * @param userName the current user
     * @throws UserForbiddenException
     */
    private void createHistoryLog(Chart chart, String userName) {
        HistoryLog historyLog = new HistoryLog();
        historyLog.setHistory(chart);
        historyLog.setCreatedBy(userName);
        historyLog.setUpdatedBy(userName);
        historyLog.setType(HistoryLogTypeEnum.chart.name());
        historyLog.setHistoryId(chart.getId());
        historyLogService.create(historyLog);

    }
}
