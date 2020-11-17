package io.etrace.api.service.graph;

import com.google.common.base.Strings;
import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.MetricResult;
import io.etrace.api.model.Target;
import io.etrace.api.model.bo.GroupResult;
import io.etrace.api.model.bo.SelectResult;
import io.etrace.api.model.graph.AppNodeQueryResult;
import io.etrace.api.model.graph.NodeType;
import io.etrace.api.model.graph.SimpleNodeQueryResult;
import io.etrace.api.model.po.ui.Chart;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.NodeMapper;
import io.etrace.api.service.AppMetricService;
import io.etrace.api.service.ChartService;
import io.etrace.api.service.MetricService;
import io.etrace.api.service.UserActionService;
import io.etrace.api.util.MetricBeanUtil;
import io.etrace.api.util.SyncUtil;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.MetricResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.*;

@Service
public class NodeService extends BaseService<Node> {

    private static final int GROUP_NODE_MAX = 10;
    private final NodeMapper nodeMapper;
    private final SimpleNodeCallback simpleNodeCallback = new SimpleNodeCallback();
    private final AppNodeCallback appNodeCallback = new AppNodeCallback();
    @Autowired
    private ChartService chartService;
    @Autowired
    private MetricService metricService;
    @Autowired
    private AppMetricService appMetricService;

    @Autowired
    public NodeService(NodeMapper nodeMapper) {
        super(nodeMapper, UserActionService.nodeCallback);
        this.nodeMapper = nodeMapper;
    }

    public void updateChartIds(Node node, ETraceUser user) throws UserForbiddenException {
        createHistoryLog(node, user, HistoryLogTypeEnum.node, true);
        nodeMapper.save(node);
    }

    @Override
    public SearchResult<Node> search(String title, String globalId, Integer pageNum, Integer pageSize, String user,
                                     String status) {
        SearchResult<Node> result = new SearchResult<>();
        result.setTotal(nodeMapper
            .countByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title, globalId, status, user, user));
        result.setResults(nodeMapper.findByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title,
            globalId, status, user, user, PageRequest.of(pageNum - 1, pageSize)));
        return result;
    }

    @Override
    public List<Node> findByIds(String title, List<Long> ids) {
        return nodeMapper.findByTitleContainingAndIdIn(title, ids);
    }

    @Override
    public Node findByGlobalId(@NotEmpty String globalConfigId) {
        return nodeMapper.findByGlobalId(globalConfigId);
    }

    @Override
    public Node findById(long id, ETraceUser user) {
        Optional<Node> op = findById(id);

        if (op.isPresent()) {
            Node node = op.get();
            List<Long> chartIds = node.getChartIds();
            if (chartIds != null && !chartIds.isEmpty()) {
                List<Chart> charts = chartService.findChartByIds(chartIds);
                Map<Long, Chart> chartMap = new HashMap<>();
                for (Chart chart : charts) {
                    chartMap.put(chart.getId(), chart);
                }
                List<Chart> sortedCharts = new ArrayList<>(charts.size());
                for (Long chartId : chartIds) {
                    sortedCharts.add(chartMap.get(chartId));
                }
                node.setCharts(sortedCharts);
            }
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (userAction != null) {
                List<Long> favorites = userAction.getFavoriteNodeIds();
                if (favorites != null && !favorites.isEmpty() && favorites.contains(node.getId())) {
                    node.setIsStar(true);
                }
            }
            return node;
        } else {
            return null;
        }
    }

    @Override
    public void syncSonMetricConfig(Node node, ETraceUser user) {
        node.setChartIds(SyncUtil.syncCharts(node.getCharts(), node.getUpdatedBy(), chartService, user));
    }

    public List<SimpleNodeQueryResult> queryNode(Node node, ETraceUser user) throws BadRequestException {
        if (NodeType.GroupNode.equals(node.getNodeType())) {
            return queryGroupNode(node, user);
        } else if (NodeType.AppNode.equals(node.getNodeType())) {
            AppNodeQueryResult appNodeQueryResult = new AppNodeQueryResult();
            queryAppNode(node, appNodeQueryResult, user);
            return Arrays.asList(appNodeQueryResult);
        } else {
            SimpleNodeQueryResult simpleNodeQueryResult = new SimpleNodeQueryResult();
            querySimpleNode(node, simpleNodeQueryResult, user);
            return Arrays.asList(simpleNodeQueryResult);
        }
    }

    private List<SimpleNodeQueryResult> queryGroupNode(Node node, ETraceUser user) throws BadRequestException {
        List<String> groupBy = node.getGroupBy();
        Map<String, Object> singleNodeConfig = node.getSingleNodeConfig();
        if (groupBy == null || groupBy.size() <= 0 || singleNodeConfig == null || singleNodeConfig.size() <= 0
            || !singleNodeConfig.containsKey(Node.GROUP_NODE_TYPE)
            || !singleNodeConfig.containsKey(Node.GROUP_NODE_NAME)) {
            throw new BadRequestException("the node " + node.getTitle() + " is not valid");
        }
        String nodeName = (String)singleNodeConfig.get(Node.GROUP_NODE_NAME);
        String appIdKey = (String)singleNodeConfig.get(Node.GROUP_NODE_APPID);
        boolean appNode = NodeType.AppNode.name().equals(singleNodeConfig.get(Node.GROUP_NODE_TYPE).toString());
        if (appNode) {
            if (Strings.isNullOrEmpty(appIdKey)) {
                throw new BadRequestException("the node " + node.getTitle() + " of appIdKey is not valid");
            }
            return doGroupByNode(node, nodeName, appIdKey, groupBy, appNodeCallback, user);
        } else {
            return doGroupByNode(node, nodeName, appIdKey, groupBy, simpleNodeCallback, user);
        }
    }

    private List<SimpleNodeQueryResult> doGroupByNode(Node node, String nodeName, String appIdKey,
                                                      List<String> nodeGroupKey, Callback callback, ETraceUser user)
        throws BadRequestException {
        Map<Map<String, String>, SimpleNodeQueryResult> nodeResults = new HashMap<>();
        SimpleNodeQueryResult simpleNodeQueryResult = new SimpleNodeQueryResult();
        querySimpleNode(node, simpleNodeQueryResult, user);
        List<MetricResult> metricResults = simpleNodeQueryResult.getResults();
        boolean nodeGroupKeyValid = nodeGroupKey != null && nodeGroupKey.size() > 0;
        for (MetricResult metricResult : metricResults) {
            SelectResult selectResult = (SelectResult)metricResult.getResult().getResults();
            List<GroupResult> groupResults = selectResult.getGroups();
            if (groupResults.size() <= 0) {
                continue;
            }
            boolean merge = nodeGroupKeyValid && groupResults.get(0).getGroup().size() > nodeGroupKey.size();
            if (merge) {
                Map<Map<String, String>, List<GroupResult>> groupMerge = new HashMap<>();
                for (GroupResult groupResult : groupResults) {
                    Map<String, String> metricGroup = groupResult.getGroup();
                    Map<String, String> nodeGroup = new HashMap<>();
                    for (String groupKey : nodeGroupKey) {
                        nodeGroup.put(groupKey, metricGroup.get(groupKey));
                    }
                    List<GroupResult> nodeMergeGroup = groupMerge.computeIfAbsent(nodeGroup, k -> new ArrayList<>());
                    nodeMergeGroup.add(groupResult);
                }
                for (Map.Entry<Map<String, String>, List<GroupResult>> entry : groupMerge.entrySet()) {
                    Map<String, String> nodeGroup = entry.getKey();
                    SimpleNodeQueryResult nodeResult = nodeResults.computeIfAbsent(nodeGroup, k -> {
                        if (nodeResults.size() >= GROUP_NODE_MAX) {
                            return null;
                        }
                        return callback.newNodeQueryResult(nodeName, nodeGroup, nodeGroup.get(appIdKey));
                    });
                    if (nodeResult == null) {
                        break;
                    }
                    nodeResult.getResults().add(
                        formMetricResultSet(selectResult, entry.getValue(), metricResult.getMetricShowName(),
                            metricResult.getChart()));
                }
            } else {
                for (GroupResult groupResult : groupResults) {
                    if (nodeResults.size() >= GROUP_NODE_MAX) {
                        break;
                    }
                    Map<String, String> group = groupResult.getGroup();
                    SimpleNodeQueryResult nodeQueryResult = callback.newNodeQueryResult(nodeName, group, null);
                    nodeQueryResult.getResults().add(
                        formMetricResultSet(selectResult, Arrays.asList(groupResult), metricResult.getMetricShowName(),
                            metricResult.getChart()));
                    nodeResults.put(groupResult.getGroup(), nodeQueryResult);
                }
            }
        }
        return new ArrayList<>(nodeResults.values());
    }

    private MetricResult formMetricResultSet(SelectResult selectResult, List<GroupResult> groupResults,
                                             String metricShowName, Chart chart) {
        MetricResult metricResult = new MetricResult();
        MetricResultSet resultSet = new MetricResultSet();
        SelectResult nodeSelectResult = new SelectResult();
        nodeSelectResult.setMeasurementName(selectResult.getMeasurementName());
        nodeSelectResult.setInterval(selectResult.getInterval());
        nodeSelectResult.setStartTime(selectResult.getStartTime());
        nodeSelectResult.setEndTime(selectResult.getEndTime());
        nodeSelectResult.setPointCount(selectResult.getPointCount());
        nodeSelectResult.setGroups(groupResults);
        resultSet.setResults(nodeSelectResult);
        metricResult.setMetricShowName(metricShowName);
        metricResult.setChart(chart);
        metricResult.setResult(resultSet);
        return metricResult;
    }

    private void queryAppNode(Node node, AppNodeQueryResult appNodeQueryResult, ETraceUser user)
        throws BadRequestException {
        if (Strings.isNullOrEmpty(node.getAppId())) {
            throw new BadRequestException("the appId of AppNode " + node.getTitle() + " can not be null");
        }
        appNodeQueryResult.setAppId(node.getAppId());
        querySimpleNode(node, appNodeQueryResult, user);
        Target target = node.getCharts().get(0).getTargets().get(0);
        int[] appEvent = appMetricService.queryApp(node.getAppId(), target.getFrom(), target.getTo(), user);
        appNodeQueryResult.setChange(appEvent[0]);
        appNodeQueryResult.setAlert(appEvent[1]);
    }

    private void querySimpleNode(Node node, SimpleNodeQueryResult simpleNodeQueryResult, ETraceUser user)
        throws BadRequestException {
        List<String> metricShowNames = new ArrayList<>();
        List<Chart> charts = new ArrayList<>();
        List<MetricBean> metricBeanList = convertTargetToMetricBean(node, metricShowNames, charts);
        simpleNodeQueryResult.setId(node.getId());
        simpleNodeQueryResult.setTitle(node.getTitle());
        simpleNodeQueryResult.setNodeType(node.getNodeType());
        if (metricBeanList.size() <= 0) {
            return;
        }
        List<MetricResultSet> metricResultSets = metricService.queryDataWithMetricBean(metricBeanList, user);
        List<MetricResult> metricResults = new ArrayList<>();
        for (int i = 0, len = metricResultSets.size(); i < len; i++) {
            MetricResult metricResult = new MetricResult();
            metricResult.setMetricShowName(metricShowNames.get(i));
            metricResult.setChart(charts.get(i));
            metricResult.setResult(metricResultSets.get(i));
            metricResults.add(metricResult);
        }
        simpleNodeQueryResult.setResults(metricResults);
    }

    private List<MetricBean> convertTargetToMetricBean(Node node, List<String> metricShowNames,
                                                       List<Chart> finalCharts) {
        List<MetricBean> metricBeanList = new ArrayList<>();
        List<Chart> charts = node.getCharts();
        for (Chart chart : charts) {
            List<Target> targets = chart.getTargets();
            if (targets != null) {
                for (Target target : targets) {
                    MetricBean metricBean = MetricBeanUtil.convert(target);
                    metricShowNames.add(chart.getTitle());
                    finalCharts.add(chart);
                    metricBeanList.add(metricBean);
                }
            }
        }
        return metricBeanList;
    }

    void initNodeQueryResult(SimpleNodeQueryResult simpleNodeQueryResult, String nodeName, Map<String, String> group) {
        String title = nodeName;
        for (Map.Entry<String, String> entry : group.entrySet()) {
            String key = "{" + entry.getKey() + "}";
            title = title.replace(key, entry.getValue());
        }
        simpleNodeQueryResult.setTitle(title);
        simpleNodeQueryResult.setResults(new ArrayList<>());
    }

    @Override
    public void updateUserFavorite(long id) {
        nodeMapper.updateUserFavorite(id);
    }

    @Override
    public void updateUserView(long id) {
        nodeMapper.updateUserView(id);
    }

    @Override
    public void deleteUserFavorite(long id) {
        nodeMapper.deleteUserFavorite(id);
    }

    public interface Callback {
        SimpleNodeQueryResult newNodeQueryResult(String nodeName, Map<String, String> group, String appId);
    }

    public class SimpleNodeCallback implements Callback {

        @Override
        public SimpleNodeQueryResult newNodeQueryResult(String nodeName, Map<String, String> group, String appId) {
            SimpleNodeQueryResult simpleNodeQueryResult = new SimpleNodeQueryResult();
            simpleNodeQueryResult.setNodeType(NodeType.SimpleNode);
            initNodeQueryResult(simpleNodeQueryResult, nodeName, group);
            simpleNodeQueryResult.setGroup(group);
            return simpleNodeQueryResult;
        }
    }

    public class AppNodeCallback implements Callback {

        @Override
        public SimpleNodeQueryResult newNodeQueryResult(String nodeName, Map<String, String> group, String appId) {
            AppNodeQueryResult appNodeQueryResult = new AppNodeQueryResult();
            appNodeQueryResult.setNodeType(NodeType.AppNode);
            initNodeQueryResult(appNodeQueryResult, nodeName, group);
            appNodeQueryResult.setAppId(appId);
            appNodeQueryResult.setGroup(group);
            return appNodeQueryResult;
        }
    }
}
