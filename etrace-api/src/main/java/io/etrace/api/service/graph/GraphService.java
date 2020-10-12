package io.etrace.api.service.graph;

import com.google.common.collect.Lists;
import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.Graph;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.GraphMapper;
import io.etrace.api.service.UserActionService;
import io.etrace.api.util.SyncUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

@Service
public class GraphService extends BaseService<Graph> {

    private final GraphMapper graphMapper;
    @Autowired
    private NodeService nodeService;

    @Autowired
    public GraphService(GraphMapper graphMapper) {
        super(graphMapper, UserActionService.graphCallback);
        this.graphMapper = graphMapper;
    }

    public void updateNodeIds(Graph graph, ETraceUser user) throws UserForbiddenException {
        createHistoryLog(graph, user, HistoryLogTypeEnum.graph, true);
        graphMapper.save(graph);
    }

    @Override
    public List<Graph> findByIds(String title, List<Long> ids) {
        return graphMapper.findByTitleContainingAndIdIn(title, ids);
    }

    @Override
    public Graph findByGlobalId(@NotEmpty String globalConfigId) {
        return graphMapper.findByGlobalId(globalConfigId);
    }

    @Override
    public SearchResult<Graph> search(String title, String globalId, Integer pageNum, Integer pageSize, String user,
                                      String status) {
        SearchResult<Graph> result = new SearchResult<>();
        result.setTotal(graphMapper
            .countByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title, globalId, status, user, user));
        result.setResults(graphMapper.findByTitleContainingAndGlobalIdAndStatusAndCreatedByOrUpdatedBy(title,
            globalId, status, user, user, PageRequest.of(pageNum - 1, pageSize)));
        return result;
    }

    @Override
    public Graph findById(long id, ETraceUser user) {
        Optional<Graph> op = findById(id);

        if (op.isPresent()) {
            Graph graph = op.get();
            List<Long> nodeIds = graph.getNodeIds();
            if (nodeIds != null && !nodeIds.isEmpty()) {
                Iterable<Node> nodes = nodeService.findByIds(nodeIds);
                graph.setNodes(Lists.newArrayList(nodes));
            }
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (userAction != null) {
                List<Long> favorites = userAction.getFavoriteGraphIds();
                if (favorites != null && !favorites.isEmpty() && favorites.contains(graph.getId())) {
                    graph.setIsStar(true);
                }
            }
            return graph;
        } else {
            return null;
        }
    }

    @Override
    public void syncSonMetricConfig(Graph graph, ETraceUser user) {
        graph.setNodeIds(SyncUtil.syncNodes(graph.getNodes(), graph.getUpdatedBy(), nodeService, user));
    }

    @Override
    public void updateUserFavorite(long id) {
        graphMapper.updateUserFavorite(id);
    }

    @Override
    public void updateUserView(long id) {
        graphMapper.updateUserView(id);
    }

    @Override
    public void deleteUserFavorite(long id) {
        graphMapper.deleteUserFavorite(id);
    }

}