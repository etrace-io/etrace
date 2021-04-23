package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.model.bo.yellowpage.SearchType;
import io.etrace.api.model.po.ui.Dashboard;
import io.etrace.api.model.po.ui.DashboardApp;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.po.yellowpage.SearchRecord;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.UserActionService;
import io.etrace.api.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/user-action")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private UserService userService;

    @GetMapping(path = {"/top/{num}", "/top"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户活动topN")
    public UserAction userAction(@PathVariable(value = "num", required = false) Integer num,
                                 @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            UserAction userAction = userActionService.searchTopUserAction(user, num);
            return userAction;
        }
        return null;
    }

    @GetMapping(value = "/view/board", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户浏览的面板")
    public SearchResult<Dashboard> userViewAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false) Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current,
        @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<Dashboard> dashboards = userActionService.searchViewByPageSize(user, pageSize, current,
                title, departmentId, productLineId);
            return dashboards;
        }
        return SearchResult.empty();
    }

    @GetMapping(value = "/view/node", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户浏览的Node")
    public SearchResult<Node> userViewNodeAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false) Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current,
        @CurrentUser ETraceUser user)
        throws Exception {
        if (!user.isAnonymousUser()) {
            SearchResult<Node> nodes = userActionService.searchViewNodeByPageSize(user, pageSize, current, title,
                departmentId, productLineId);
            return nodes;
        }
        return SearchResult.empty();
    }

    @GetMapping(value = "/view/graph", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户浏览的大盘")
    public SearchResult<Graph> userViewGraphAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false) Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current,
        @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<Graph> graphs = userActionService.searchViewGraphByPageSize(user, pageSize, current, title,
                departmentId, productLineId);
            return graphs;
        }
        return SearchResult.empty();
    }

    @GetMapping(value = "/favorite/board", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户收藏的面板")
    public SearchResult<Dashboard> userFavoriteAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false)
            Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current,
        @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<Dashboard> dashboards = userActionService.searchFavoriteByPageSize(user, pageSize, current,
                title, departmentId, productLineId);
            return dashboards;
        }
        return SearchResult.empty();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/favorite/app")
    @ApiOperation("用户收藏的App")
    public SearchResult<DashboardApp> userAppAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false) Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current,
        @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<DashboardApp> dashboards = userActionService.searchAppByPageSize(user, pageSize, current,
                title, departmentId, productLineId);
            return dashboards;
        }
        return SearchResult.empty();
    }

    @GetMapping(value = "/favorite/node", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户收藏的Node")
    public SearchResult<Node> userFavoriteNodeAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false) Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<Node> nodes = userActionService.searchFavoriteNodeByPageSize(user, pageSize, current,
                title, departmentId, productLineId);
            return nodes;
        }
        return SearchResult.empty();
    }

    @GetMapping(value = "/favorite/graph", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户收藏的Graph")
    public SearchResult<Graph> userFavoriteGraphAction(
        @RequestParam(value = "departmentId", required = false) Long departmentId,
        @RequestParam(value = "productLineId", required = false) Long productLineId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer current, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<Graph> graphs = userActionService.searchFavoriteGraphByPageSize(user, pageSize, current,
                title, departmentId, productLineId);
            return graphs;
        }
        return SearchResult.empty();
    }

    @GetMapping(value = "/favorite/recordAndList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户收藏的record和list")
    public void userFavoriteRecordAction(@RequestParam(value = "title", required = false) String title,
                                         @RequestParam(value = "pageSize", defaultValue = "10")
                                             Integer pageSize,
                                         @RequestParam(value = "current", defaultValue = "1") Integer current,
                                         @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            SearchResult<SearchRecord> records = userActionService.searchFavoriteRecordsByPageSize(user, pageSize,
                current, title);
            SearchResult<SearchList> lists = userActionService.searchFavoriteListsByPageSize(user, pageSize,
                current, title);
            Map<String, SearchResult> result = new LinkedHashMap<>(2);
            result.put(SearchType.LIST.name(), lists);
            result.put(SearchType.RECORD.name(), records);
        }
    }

    @PutMapping(produces = MediaType.ALL_VALUE, value = "/favorite/board/{boardId}")
    @ApiOperation("创建或更新用户收藏面板")
    public void createOrUpdateFavorite(@PathVariable("boardId") Long boardId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateFavorite(boardId, user);
        }
    }

    @PutMapping(produces = MediaType.ALL_VALUE, value = "/favorite/node/{nodeId}")
    @ApiOperation("创建或更新用户收藏Node")
    public void createOrUpdateNodeFavorite(@PathVariable("nodeId") Long nodeId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateNodeFavorite(nodeId, user);
        }
    }

    @PutMapping(produces = MediaType.ALL_VALUE, value = "/favorite/graph/{graphId}")
    @ApiOperation("创建或更新用户收藏大盘")
    public void createOrUpdateGraphFavorite(@PathVariable("graphId") Long graphId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateGraphFavorite(graphId, user);
        }
    }

    @PutMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/app/{appId}")
    @ApiOperation("更新用户收藏App数据")
    public void createOrUpdateApp(@PathVariable("appId") Long appId, @CurrentUser ETraceUser user) throws Exception {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateApps(appId, user);
        }
    }

    @PutMapping(produces = MediaType.ALL_VALUE, value = "/favorite/record/{recordId}")
    @ApiOperation("创建或更新用户收藏record")
    public void createOrUpdateRecordsFavorite(@PathVariable("recordId") Long recordId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateRecordsFavorite(recordId, user);
        }
    }

    @PutMapping(produces = MediaType.ALL_VALUE, value = "/favorite/list/{listId}")
    @ApiOperation("创建或更新用户收藏list")
    public void createOrUpdateListsFavorite(@PathVariable("listId") Long listId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateListsFavorite(listId, user);
        }
    }

    @PutMapping(consumes = MediaType.ALL_VALUE, value = "/view/{boardId}")
    @ApiOperation("更新用户访问数据")
    public void createOrUpdateView(@PathVariable("boardId") Long boardId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateView(boardId, user);
        }
    }

    @PutMapping(consumes = MediaType.ALL_VALUE, value = "/graph_view/{graphId}")
    @ApiOperation("更新用户访问Graph数据")
    public void createOrUpdateGraphView(@PathVariable("graphId") Long graphId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateGraphView(graphId, user);
        }
    }

    @PutMapping(consumes = MediaType.ALL_VALUE, value = "/node_view/{nodeId}")
    @ApiOperation("更新用户访问Node数据")
    public void createOrUpdateNodeView(@PathVariable("nodeId") Long nodeId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateNodeView(nodeId, user);
        }
    }

    @PutMapping(consumes = MediaType.ALL_VALUE, value = "/record_view/{recordId}")
    @ApiOperation("更新用户访问record数据")
    public void createOrUpdateRecordView(@PathVariable("recordId") Long recordId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.createOrUpdateRecordView(recordId, user);
        }
    }

    @DeleteMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/board/{boardId}")
    @ApiOperation("用户取消收藏面板")
    public void deleteFavoriteUserAction(@PathVariable("boardId") Long boardId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.deleteFavoriteUserAction(boardId, user);
        }
    }

    @DeleteMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/app/{appId}")
    @ApiOperation("用户取消收藏App")
    public void deleteAppUserAction(@PathVariable("appId") Long appId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.deleteAppUserAction(appId, user);
        }
    }

    @DeleteMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/graph/{graphId}")
    @ApiOperation("用户取消收藏Graph")
    public void deleteGraphUserAction(@PathVariable("graphId") Long graphId, @CurrentUser ETraceUser user)
        throws Exception {
        if (!user.isAnonymousUser()) {
            userActionService.deleteGraphFavoriteUserAction(graphId, user);
        }
    }

    @DeleteMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/node/{nodeId}")
    @ApiOperation("用户取消收藏Node")
    public void deleteNodeUserAction(@PathVariable("nodeId") Long nodeId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.deleteNodeFavoriteUserAction(nodeId, user);
        }
    }

    @DeleteMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/record/{recordId}")
    @ApiOperation("用户取消收藏record")
    public void deleteRecordFavoriteUserAction(@PathVariable("recordId") Long recordId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.deleteRecordFavoriteUserAction(recordId, user);
        }
    }

    @DeleteMapping(consumes = MediaType.ALL_VALUE, value = "/favorite/list/{listId}")
    @ApiOperation("用户取消收藏list")
    public void deleteListFavoriteUserAction(@PathVariable("listId") Long listId, @CurrentUser ETraceUser user) {
        if (!user.isAnonymousUser()) {
            userActionService.deleteListFavoriteUserAction(listId, user);
        }
    }
}
