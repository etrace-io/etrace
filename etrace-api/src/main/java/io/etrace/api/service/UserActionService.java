package io.etrace.api.service;

import com.google.common.collect.Lists;
import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.po.ui.Graph;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.model.vo.ui.DashboardAppVO;
import io.etrace.api.repository.UserActionMapper;
import io.etrace.api.service.base.BaseService;
import io.etrace.api.service.base.FavoriteAndViewInterface;
import io.etrace.api.service.graph.GraphService;
import io.etrace.api.service.graph.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class UserActionService {

    public final static DashboardCallback dashboardCallback = new DashboardCallback();
    public final static DashboardAppCallback dashboardAppCallback = new DashboardAppCallback();
    public final static GraphCallback graphCallback = new GraphCallback();
    public final static NodeCallback nodeCallback = new NodeCallback();
    private final static int VIEW_COUNT = 20;
    @Autowired
    private UserActionMapper userActionMapper;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private DashboardAppService dashboardAppService;
    @Autowired
    private GraphService graphService;
    @Autowired
    private NodeService nodeService;

    private void create(UserAction userAction) {
        userActionMapper.save(userAction);
    }

    private <T extends BaseItem> void doCreateOrUpdateFavorite(Long id, ETraceUser user,
                                                               Callback callback,
                                                               FavoriteAndViewInterface favoriteAndViewInterface) {
        UserAction userAction = findFavoriteByUser(user);
        if (userAction != null) {
            List<Long> favorites = callback.get(userAction, false);
            if (favorites == null) {
                favorites = new LinkedList<>();
            }
            if (!favorites.contains(id)) {
                favorites.add(0, id);
                favoriteAndViewInterface.updateUserFavorite(id);
            }
            callback.set(userAction, favorites, false);
            callback.save(userActionMapper, userAction);
        } else {
            userAction = new UserAction();
            userAction.setUserEmail(user.getEmail());
            List<Long> ids = Lists.newLinkedList();
            ids.add(id);
            callback.set(userAction, ids, false);
            create(userAction);
            favoriteAndViewInterface.updateUserFavorite(id);
        }
    }

    public void createOrUpdateFavorite(Long boardId, ETraceUser user) {
        doCreateOrUpdateFavorite(boardId, user, dashboardCallback, dashboardService);
    }

    public void createOrUpdateApps(Long appId, ETraceUser user) {
        doCreateOrUpdateFavorite(appId, user, dashboardAppCallback, dashboardAppService);
    }

    public void createOrUpdateGraphFavorite(Long graphId, ETraceUser user) {
        doCreateOrUpdateFavorite(graphId, user, graphCallback, graphService);
    }

    public void createOrUpdateNodeFavorite(Long nodeId, ETraceUser user) {
        doCreateOrUpdateFavorite(nodeId, user, nodeCallback, nodeService);
    }

    public void createOrUpdateRecordsFavorite(Long recordId, ETraceUser user) {
        // todo: implement this!
        throw new RuntimeException("==createOrUpdateRecordsFavorite== not implemented yet!");
        //doCreateOrUpdateFavorite(recordId, user, searchRecordCallback, searchRecordService);
    }

    public void createOrUpdateListsFavorite(Long listId, ETraceUser user) {
        // todo: implement this!
        throw new RuntimeException("==createOrUpdateListsFavorite== not implemented yet!");
        //doCreateOrUpdateFavorite(listId, user, searchListCallback, searchListService);
    }

    private <T extends BaseItem> void doCreateOrUpdateView(Long id, ETraceUser user, Callback callback,
                                                           FavoriteAndViewInterface favoriteAndViewInterface) {
        UserAction userAction = findFavoriteByUser(user);
        if (userAction != null) {
            callback.set(userAction, updateIds(callback.get(userAction, true), id), true);
            callback.save(userActionMapper, userAction);
        } else {
            userAction = new UserAction();
            userAction.setUserEmail(user.getEmail());
            List<Long> ids = Lists.newLinkedList();
            ids.add(0, id);
            callback.set(userAction, ids, true);
            create(userAction);
        }
        favoriteAndViewInterface.updateUserView(id);
    }

    public void createOrUpdateView(Long boardId, ETraceUser user) {
        doCreateOrUpdateView(boardId, user, dashboardCallback, dashboardService);
    }

    public void createOrUpdateGraphView(Long graphId, ETraceUser user) {
        doCreateOrUpdateView(graphId, user, graphCallback, graphService);
    }

    public void createOrUpdateNodeView(Long nodeId, ETraceUser user) {
        doCreateOrUpdateView(nodeId, user, nodeCallback, nodeService);
    }

    public void createOrUpdateRecordView(Long recordId, ETraceUser user) {
        // todo: implement this!
        throw new RuntimeException("==createOrUpdateRecordView== not implemented yet!");
        //doCreateOrUpdateView(recordId, user, searchRecordCallback, searchRecordService);
    }

    private List<Long> updateIds(List<Long> oldIds, long currentId) {
        if (oldIds == null) {
            oldIds = new LinkedList<>();
        }
        int index = -1;
        for (int i = 0; i < oldIds.size(); i++) {
            if (oldIds.get(i).equals(currentId)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            oldIds.remove(index);
            oldIds.add(0, currentId);
        } else {
            oldIds.add(0, currentId);
            if (oldIds.size() >= VIEW_COUNT) {
                oldIds = oldIds.subList(0, VIEW_COUNT);
            }
        }
        return oldIds;
    }

    private <T extends BaseItem> void doDeleteFavoriteUserAction(Long id, ETraceUser user,
                                                                 Callback callback,
                                                                 FavoriteAndViewInterface favoriteAndViewInterface) {
        UserAction favorite = findFavoriteByUser(user);
        if (favorite != null) {
            List<Long> fas = callback.get(favorite, false);
            if (fas != null && fas.contains(id)) {
                fas.remove(id);
                callback.set(favorite, fas, false);
                callback.delete(userActionMapper, favorite);
                favoriteAndViewInterface.deleteUserFavorite(id);
            }
        }
    }

    public void deleteFavoriteUserAction(Long boardId, ETraceUser user) {
        doDeleteFavoriteUserAction(boardId, user, dashboardCallback, dashboardService);
    }

    public void deleteAppUserAction(Long appId, ETraceUser user) {
        doDeleteFavoriteUserAction(appId, user, dashboardAppCallback, dashboardAppService);
    }

    public void deleteGraphFavoriteUserAction(Long graphId, ETraceUser user) {
        doDeleteFavoriteUserAction(graphId, user, graphCallback, graphService);
    }

    public void deleteNodeFavoriteUserAction(Long nodeId, ETraceUser user) {
        doDeleteFavoriteUserAction(nodeId, user, nodeCallback, nodeService);
    }

    public void deleteRecordFavoriteUserAction(Long recordId, ETraceUser user) {
        // todo: implement this!
        throw new RuntimeException("==deleteRecordFavoriteUserAction== not implemented yet!");
        //doDeleteFavoriteUserAction(recordId, user, searchRecordCallback, searchRecordService);
    }

    public void deleteListFavoriteUserAction(Long listId, ETraceUser user) {
        // todo: implement this!
        throw new RuntimeException("==deleteListFavoriteUserAction== not implemented yet!");
        //doDeleteFavoriteUserAction(listId, user, nodeCallback, searchRecordService);
    }

    public UserAction findFavoriteByUser(ETraceUser user) {
        return userActionMapper.findByUserEmail(user.getEmail());
    }

    public SearchResult<DashboardAppVO> searchAppByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                            String title, Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine,
            dashboardAppCallback, dashboardAppService, false);
    }

    public SearchResult<DashboardVO> searchFavoriteByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                              String title, Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine, dashboardCallback,
            dashboardService, false);
    }

    public SearchResult<Node> searchFavoriteNodeByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                           String title, Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine, nodeCallback,
            nodeService, false);
    }

    public SearchResult<Graph> searchFavoriteGraphByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                             String title, Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine, graphCallback,
            graphService, false);
    }

    public SearchResult<DashboardVO> searchViewByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                          String title,
                                                          Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine, dashboardCallback,
            dashboardService, true);
    }

    public SearchResult<Node> searchViewNodeByPageSize(ETraceUser user, Integer pageSize, Integer current, String title,
                                                       Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine, nodeCallback,
            nodeService, true);
    }

    public SearchResult<Graph> searchViewGraphByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                         String title, Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine, graphCallback,
            graphService, true);
    }

    private <VO extends BaseItem, T extends BaseItem> SearchResult<VO> doSearchFavoriteByPageSize(ETraceUser user,
                                                                            Integer pageSize,
                                                                            Integer current,
                                                                            String title,
                                                                            Long department,
                                                                            Long productLine,
                                                                            Callback callback,
                                                                            BaseService<VO, T> baseService,
                                                                            boolean view) {
        SearchResult<VO> searchResult = new SearchResult<>();
        searchResult.setTotal(0);
        UserAction userAction = userActionMapper.findByUserEmail(user.getEmail());
        if (userAction != null) {
            List<Long> appIds = Lists.newLinkedList();
            List<Long> favorites = callback.get(userAction, view);
            if (favorites != null) {
                int fromIndex = (current - 1) * pageSize;
                int toIndex = current * pageSize;
                searchResult.setTotal(favorites.size());
                if (favorites.size() > toIndex) {
                    appIds.addAll(favorites.subList(fromIndex, toIndex));
                } else {
                    appIds.addAll(favorites.subList(fromIndex, favorites.size()));
                }
            }
            if (!appIds.isEmpty()) {
                List<VO> entities = baseService.findByIds(title, Lists.newArrayList(appIds));
                if (entities != null && !entities.isEmpty()) {
                    entityIsStar(favorites, entities);
                    searchResult.setResults(entities);
                }
            }
        }
        return searchResult;
    }

    private <T extends BaseItem> void entityIsStar(List<Long> views, List<T> entities) {
        for (T t : entities) {
            if (views.contains(t.getId())) {
                t.setIsStar(true);
            }
        }
    }

    public UserAction searchTopUserAction(ETraceUser user, Integer topN) {
        UserAction userAction = userActionMapper.findByUserEmail(user.getEmail());
        if (userAction != null) {
            List<Long> views = userAction.getViewBoardIds();
            List<Long> favorites = userAction.getFavoriteBoardIds();
            if (null == topN) {
                userAction.setViewBoardIds(views);
                userAction.setFavoriteBoardIds(favorites);
            } else {
                if (views != null && views.size() > topN) {
                    userAction.setViewBoardIds(views.subList(0, topN));
                }
                if (favorites != null && favorites.size() > topN) {
                    userAction.setFavoriteBoardIds(favorites.subList(0, topN));
                }
            }

            if (userAction.getViewBoardIds() != null && !userAction.getViewBoardIds().isEmpty()) {
                List<DashboardVO> viewDashboards = dashboardService.findByIds(null, null, null,
                    userAction.getViewBoardIds());
                userAction.setViewBoards(viewDashboards);
            }
            if (userAction.getFavoriteBoardIds() != null && !userAction.getFavoriteBoardIds().isEmpty()) {
                List<DashboardVO> favoriteDashboards = dashboardService.findByIds(null, null, null,
                    userAction.getFavoriteBoardIds());
                userAction.setFavoriteBoards(favoriteDashboards);
            }
        }
        return userAction;
    }

    public interface Callback {
        List<Long> get(UserAction userAction, boolean view);

        void set(UserAction userAction, List<Long> ids, boolean view);

        default void save(UserActionMapper userActionMapper, UserAction userAction) {
            userActionMapper.save(userAction);
        }

        default void delete(UserActionMapper userActionMapper, UserAction userAction) {
            userActionMapper.deleteById(userAction.getId());
        }
    }

    public static class DashboardCallback implements Callback {

        @Override
        public List<Long> get(UserAction userAction, boolean view) {
            if (view) {
                return userAction.getViewBoardIds();
            } else {
                return userAction.getFavoriteBoardIds();
            }
        }

        @Override
        public void set(UserAction userAction, List<Long> ids, boolean view) {
            userAction.setFavoriteBoardIds(ids);
            if (view) {
                userAction.setViewBoardIds(ids);
            } else {
                userAction.setFavoriteBoardIds(ids);
            }
        }
    }

    public static class DashboardAppCallback implements Callback {

        @Override
        public List<Long> get(UserAction userAction, boolean view) {
            return userAction.getFavoriteApps();
        }

        @Override
        public void set(UserAction userAction, List<Long> ids, boolean view) {
            userAction.setFavoriteApps(ids);
        }
    }

    public static class GraphCallback implements Callback {

        @Override
        public List<Long> get(UserAction userAction, boolean view) {
            if (view) {
                return userAction.getViewGraphIds();
            } else {
                return userAction.getFavoriteGraphIds();
            }
        }

        @Override
        public void set(UserAction userAction, List<Long> ids, boolean view) {
            if (view) {
                userAction.setViewGraphIds(ids);
            } else {
                userAction.setFavoriteGraphIds(ids);
            }
        }
    }

    public static class NodeCallback implements Callback {

        @Override
        public List<Long> get(UserAction userAction, boolean view) {
            if (view) {
                return userAction.getViewNodeIds();
            } else {
                return userAction.getFavoriteNodeIds();
            }
        }

        @Override
        public void set(UserAction userAction, List<Long> ids, boolean view) {
            if (view) {
                userAction.setViewNodeIds(ids);
            } else {
                userAction.setFavoriteNodeIds(ids);
            }
        }
    }
}
