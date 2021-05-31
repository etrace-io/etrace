package io.etrace.api.service.graph;

import com.google.common.collect.Lists;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.ui.Graph;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.GraphMapper;
import io.etrace.api.service.UserActionService;
import io.etrace.api.service.base.BaseService;
import io.etrace.api.util.SyncUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GraphService extends BaseService<Graph, Graph> {

    private GraphMapper graphMapper;
    @Autowired
    private NodeService nodeService;

    @Autowired
    public GraphService(GraphMapper graphMapper) {
        super(graphMapper, UserActionService.graphCallback);
        this.graphMapper = graphMapper;
    }

    //public void updateNodeIds(Graph graph) throws UserForbiddenException {
    //    createHistoryLog(graph, graph.getUpdatedBy(), true);
    //    graphMapper.updateNodeIds(graph);
    //}

    @Override
    public List<Graph> findByIds(String title, List<Long> ids) {
        return null;
    }

    @Override
    public Graph findById(long id, ETraceUser user) {
        Optional<Graph> op = findById(id);
        if (op.isPresent()) {
            Graph graph = op.get();
            List<Long> nodeIds = graph.getNodeIds();
            if (nodeIds != null && !nodeIds.isEmpty()) {
                List<Node> nodes = Lists.newArrayList(nodeService.findByIds(nodeIds));
                graph.setNodes(nodes);
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
    public <S extends Graph> void syncSonMetricConfig(S t, ETraceUser user) {

    }
    public void syncSonMetricConfig(Graph graph) {
        graph.setNodeIds(SyncUtil.syncNodes(graph.getNodes(), graph.getUpdatedBy(), nodeService));
    }
    @Override
    public SearchResult<Graph> search(String title, String globalId, Integer pageNum, Integer pageSize, String user,
                                      String status) {
        return null;
    }



    @Override
    public void updateUserFavorite(long id) {

    }

    @Override
    public void updateUserView(long id) {

    }

    @Override
    public void deleteUserFavorite(long id) {

    }

    @Override
    public Graph findByGlobalId(String globalConfigId) {
        return null;
    }
}