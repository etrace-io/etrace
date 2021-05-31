package io.etrace.api.util;

import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.vo.ui.ChartVO;
import io.etrace.api.service.ChartService;
import io.etrace.api.service.graph.NodeService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lg Date: 2019-12-02 Time: 15:40
 */
public class SyncUtil {

    public static List<Long> syncCharts(List<ChartVO> charts, String updateBy, ChartService chartService) {
        if (charts != null && !CollectionUtils.isEmpty(charts)) {
            List<String> chartGlobalIdList = new ArrayList<>();
            List<Long> chartIdList = new ArrayList<>();
            charts.forEach(chart -> {
                chartGlobalIdList.add(chart.getGlobalId());
                chart.setUpdatedBy(updateBy);
                chart.setCreatedBy(updateBy);
                try {
                    chartService.syncMetricConfig(chart, null);
                } catch (UserForbiddenException e) {
                    throw new RuntimeException(e.getMessage());
                }
            });
            chartGlobalIdList.forEach(globalId -> {
                if (StringUtils.isEmpty(globalId)) {
                    throw new RuntimeException("the chart global id is null, sync error!");
                }
                ChartVO chart = chartService.findByGlobalId(globalId);
                if (null != chart) {
                    chartIdList.add(chart.getId());
                } else {
                    throw new RuntimeException("the chart is not sync in current env,the chart global id:" + globalId);
                }
            });
            return chartIdList;
        }
        return null;
    }

    public static List<Long> syncNodes(List<Node> nodes, String updateBy, NodeService nodeService) {
        if (nodes != null && !CollectionUtils.isEmpty(nodes)) {
            List<String> nodeGlobalIdList = new ArrayList<>();
            List<Long> nodeIdList = new ArrayList<>();
            nodes.forEach(node -> {
                nodeGlobalIdList.add(node.getGlobalId());
                node.setUpdatedBy(updateBy);
                node.setCreatedBy(updateBy);
                try {
                    nodeService.syncMetricConfig(node, null);
                } catch (UserForbiddenException e) {
                    throw new RuntimeException(e.getMessage());
                }
            });
            nodeGlobalIdList.forEach(globalId -> {
                if (StringUtils.isEmpty(globalId)) {
                    throw new RuntimeException("the node global id is null, sync error!");
                }
                Node node = nodeService.findByGlobalId(globalId);
                if (null != node) {
                    nodeIdList.add(node.getId());
                } else {
                    throw new RuntimeException("the node is not sync in current env,the node global id:" + globalId);
                }
            });
            return nodeIdList;
        }
        return null;
    }
}
