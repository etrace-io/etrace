package io.etrace.api.service;

import com.google.common.collect.Lists;
import io.etrace.api.model.po.BaseVisualizationObject;
import io.etrace.api.model.po.ui.Dashboard;
import io.etrace.api.model.po.ui.DashboardApp;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.po.yellowpage.SearchRecord;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.UserActionMapper;
import io.etrace.api.service.base.FavoriteAndViewInterface;
import io.etrace.api.service.graph.BaseService;
import io.etrace.api.service.yellowpage.SearchListService;
import io.etrace.api.service.yellowpage.SearchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@Service
public class UserActionService {

    public final static DashboardCallback dashboardCallback = new DashboardCallback();
    public final static DashboardAppCallback dashboardAppCallback = new DashboardAppCallback();
    public final static GraphCallback graphCallback = new GraphCallback();
    public final static NodeCallback nodeCallback = new NodeCallback();
    public final static SearchRecordCallback searchRecordCallback = new SearchRecordCallback();
    public final static SearchListCallback searchListCallback = new SearchListCallback();
    private final static int VIEW_COUNT = 20;
    @Autowired
    private UserActionMapper userActionMapper;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private DashboardAppService dashboardAppService;


    private void create(UserAction userAction) {
        userActionMapper.save(userAction);
    }

    private <T extends BaseVisualizationObject> void doCreateOrUpdateFavorite(Long id, ETraceUser user,
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
        doCreateOrUpdateFavorite(recordId, user, searchRecordCallback, searchRecordService);
    }

    public void createOrUpdateListsFavorite(Long listId, ETraceUser user) {
        doCreateOrUpdateFavorite(listId, user, searchListCallback, searchListService);
    }

    private <T extends BaseVisualizationObject> void doCreateOrUpdateView(Long id, ETraceUser user, Callback callback,
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
        doCreateOrUpdateView(recordId, user, searchRecordCallback, searchRecordService);
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

    private <T extends BaseVisualizationObject> void doDeleteFavoriteUserAction(Long id, ETraceUser user,
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
        doDeleteFavoriteUserAction(recordId, user, searchRecordCallback, searchRecordService);
    }

    public void deleteListFavoriteUserAction(Long listId, ETraceUser user) {
        doDeleteFavoriteUserAction(listId, user, nodeCallback, searchRecordService);
    }

    public UserAction findFavoriteByUser(ETraceUser user) {
        return userActionMapper.findByUserEmail(user.getEmail());
    }

    public SearchResult<DashboardApp> searchAppByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                          String title, Long department, Long productLine) {
        return doSearchFavoriteByPageSize(user, pageSize, current, title, department, productLine,
            dashboardAppCallback, dashboardAppService, false);
    }

    public SearchResult<Dashboard> searchFavoriteByPageSize(ETraceUser user, Integer pageSize, Integer current,
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

    public SearchResult<Dashboard> searchViewByPageSize(ETraceUser user, Integer pageSize, Integer current,
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

    private <T extends BaseVisualizationObject> SearchResult<T> doSearchFavoriteByPageSize(ETraceUser user,
                                                                                           Integer pageSize,
                                                                                           Integer current,
                                                                                           String title,
                                                                                           Long department,
                                                                                           Long productLine,
                                                                                           Callback callback,
                                                                                           BaseService<T> baseService,
                                                                                           boolean view) {
        SearchResult<T> searchResult = new SearchResult<>();
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
                List<T> entities = baseService.findByIds(title, Lists.newArrayList(appIds));
                if (entities != null && !entities.isEmpty()) {
                    entityIsStar(favorites, entities);
                    searchResult.setResults(entities);
                }
            }
        }
        return searchResult;
    }

    private <T extends BaseVisualizationObject> void entityIsStar(List<Long> views, List<T> entities) {
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
                List<Dashboard> viewDashboards = dashboardService.findByIds(null, null, null,
                    userAction.getViewBoardIds());
                userAction.setViewBoards(viewDashboards);
            }
            if (userAction.getFavoriteBoardIds() != null && !userAction.getFavoriteBoardIds().isEmpty()) {
                List<Dashboard> favoriteDashboards = dashboardService.findByIds(null, null, null,
                    userAction.getFavoriteBoardIds());
                userAction.setFavoriteBoards(favoriteDashboards);
            }
        }
        return userAction;
    }

    public SearchResult<SearchRecord> searchFavoriteRecordsByPageSize(ETraceUser user, Integer pageSize,
                                                                      Integer current, String title) {

        UserAction userAction = userActionMapper.findByUserEmail(user.getEmail());
        List<Long> recordIds = userAction.getFavoriteRecordIds();

        SearchResult<SearchRecord> searchResult = new SearchResult<>();
        searchResult.setTotal(0);

        if (!CollectionUtils.isEmpty(recordIds)) {
            searchResult.setTotal(recordIds.size());
            searchResult.setResults(searchRecordService.findByIdListWithPage(recordIds, null, current, pageSize));
            searchResult.getResults().stream().forEach(searchRecord -> searchRecord.setStar(Boolean.TRUE));
            searchRecordService.findExtendIcon(searchResult.getResults());
        }
        return searchResult;

    }

    public SearchResult<SearchList> searchFavoriteListsByPageSize(ETraceUser user, Integer pageSize, Integer current,
                                                                  String title) {
        UserAction userAction = userActionMapper.findByUserEmail(user.getEmail());
        SearchResult<SearchList> searchResult = new SearchResult<>();
        searchResult.setTotal(0);
        List<Long> listIds = userAction.getFavoriteListIds();
        if (!CollectionUtils.isEmpty(listIds)) {
            searchResult.setTotal(listIds.size());
            int fromIndex = (current - 1) * pageSize;
            int toIndex = current * pageSize;
            searchResult.setTotal(listIds.size());
            if (listIds.size() > toIndex) {
                searchResult.setResults(searchListService.findByIdList(listIds.subList(fromIndex, toIndex)));
            } else {
                searchResult.setResults(searchListService.findByIdList(listIds.subList(fromIndex, listIds.size())));
            }
            searchResult.getResults().stream().forEach(searchList -> searchList.setStar(Boolean.TRUE));
        }
        return searchResult;
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

    public static class SearchRecordCallback implements Callback {

        @Override
        public List<Long> get(UserAction userAction, boolean view) {
            return userAction.getFavoriteRecordIds();
        }

        @Override
        public void set(UserAction userAction, List<Long> ids, boolean view) {

            userAction.setFavoriteRecordIds(ids);
        }
    }

    public static class SearchListCallback implements Callback {

        @Override
        public List<Long> get(UserAction userAction, boolean view) {
            return userAction.getFavoriteListIds();
        }

        @Override
        public void set(UserAction userAction, List<Long> ids, boolean view) {
            userAction.setFavoriteListIds(ids);
        }
    }
}
