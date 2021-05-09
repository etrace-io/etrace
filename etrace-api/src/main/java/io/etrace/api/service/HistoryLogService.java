package io.etrace.api.service;

import com.google.common.collect.Lists;
import io.etrace.api.model.po.ui.HistoryLog;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.model.vo.ui.DashboardAppVO;
import io.etrace.api.repository.HistoryLogMapper;
import io.etrace.common.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Optional;

@Service
public class HistoryLogService {
    @Autowired
    private HistoryLogMapper historyLogMapper;

    @Autowired
    private ChartService chartService;

    @Autowired
    private DashboardService dashboardService;

    public Long create(HistoryLog historyLog) {
        if (null == historyLog.getHistory()) {
            return null;
        }
        historyLogMapper.save(historyLog);
        return historyLog.getId();
    }

    public Optional<HistoryLog> findById(Long id) {
        return historyLogMapper.findById(id);
    }

    /**
     * find extend info for history log
     *
     * @param id
     * @return
     */
    public Optional<HistoryLog> findExtendInfo(Long id) throws IOException {
        Optional<HistoryLog> op = findById(id);
        if (op.isPresent()) {
            addMoreInfo(op.get());
        }
        return op;
    }

    public SearchResult<HistoryLog> search(String type, Long id, int pageNum, int pageSize) {

        SearchResult<HistoryLog> searchResult = new SearchResult<>();
        int count = historyLogMapper.countByHistoryIdAndType(id, type);
        searchResult.setTotal(count);
        if (count > 0) {
            Integer start = (pageNum - 1) * pageSize;
            PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
            searchResult.setResults(historyLogMapper.findByHistoryIdAndType(id, type, pageRequest));
        }
        return searchResult;
    }

    /**
     * find more info for the history
     *
     * @param historyLog
     */
    private void addMoreInfo(HistoryLog historyLog) throws IOException {
        switch (historyLog.getType()) {
            case "dashboard":
                DashboardVO dashboard = JSONUtil.toObject(JSONUtil.toString(historyLog.getHistory()), DashboardVO.class);
                if (!CollectionUtils.isEmpty(dashboard.getChartIds())) {
                    dashboard.setCharts(chartService.findChartByIds(dashboard.getChartIds()));
                    historyLog.setHistory(dashboard);
                }
                return;
            case "dashboardApp":
                DashboardAppVO dashboardApp = JSONUtil.toObject(JSONUtil.toString(historyLog.getHistory()),
                    DashboardAppVO.class);
                if (!CollectionUtils.isEmpty(dashboardApp.getDashboardIds())) {
                    dashboardApp.setDashboards(
                        Lists.newArrayList(dashboardService.findByIds(dashboardApp.getDashboardIds())));
                    historyLog.setHistory(dashboardApp);
                }
                return;
            default:
                // ignore
                return;

        }

    }
}
