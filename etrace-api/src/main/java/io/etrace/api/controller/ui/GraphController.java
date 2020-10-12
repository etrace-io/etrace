package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.Graph;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.UserActionService;
import io.etrace.api.service.graph.GraphService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/graph")
@Api(tags = {MYSQL_DATA})
public class GraphController extends BaseController<Graph> {

    private final GraphService graphService;
    private final UserActionService userActionService;

    @Autowired
    public GraphController(GraphService graphService, UserActionService userActionService) {
        super(graphService);
        this.graphService = graphService;
        this.userActionService = userActionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("创建大盘")
    public Long create(@RequestBody Graph graph, @CurrentUser ETraceUser user) throws Exception {
        try {
            return doCreate(graph, user);
        } catch (Exception e) {
            throw new BadRequestException("创建大盘异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("大盘查询")
    public SearchResult<Graph> search(@RequestParam(value = "user", required = false) String user,
                                      @RequestParam(value = "departmentId", required = false) Long departmentId,
                                      @RequestParam(value = "productLineId", required = false) Long productLineId,
                                      @RequestParam(value = "title", required = false) String title,
                                      @RequestParam(value = "globalId", required = false) String globalId,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                      @RequestParam(value = "current", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "status", defaultValue = "Active") String status,
                                      @CurrentUser ETraceUser eTraceUser)
        throws Exception {
        try {
            return doSearch(user, title, globalId, pageSize, pageNum, status, eTraceUser);
        } catch (Exception e) {
            throw new BadRequestException("大盘查询异常：" + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("获取某个大盘的详细信息")
    public Graph findById(@PathVariable("id") long id, @CurrentUser ETraceUser eTraceUser) throws Exception {
        try {
            return doFindById(id, eTraceUser);
        } catch (Exception e) {
            throw new BadRequestException("获取看板的详细信息异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("更新大盘")
    public void update(@RequestBody Graph graph, @CurrentUser ETraceUser user) throws Exception {
        try {
            doUpdate(graph, user);
        } catch (Exception e) {
            throw new BadRequestException("更新大盘异常：" + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/nodes", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("更新大盘下的Node")
    public ResponseEntity updateNodeIds(@PathVariable("id") long graphId, @RequestBody Graph graph,
                                        @CurrentUser ETraceUser user) throws Exception {
        try {
            if (graph == null) {
                graph = new Graph();
            }
            if (graph.getNodeIds() == null) {
                graph.setNodeIds(Collections.emptyList());
            }
            graph.setId(graphId);
            graph.setUpdatedBy(user.getUsername());
            graphService.updateNodeIds(graph, user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("更新大盘下的Node异常：" + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除大盘")
    public void deleteGraph(@PathVariable("id") long id,
                            @RequestParam(value = "status", defaultValue = "Inactive") String status,
                            @CurrentUser ETraceUser user) throws Exception {
        try {
            doDelete(id, status, user);
        } catch (Exception e) {
            throw new BadRequestException("删除大盘异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/sync")
    @ApiOperation("同步大盘")
    public void syncGraph(@RequestBody Graph graph, @CurrentUser ETraceUser user) throws Exception {
        graphService.syncMetricConfig(graph, user);
    }

    @Override
    protected void createOrUpdateView(long id, ETraceUser user) {
        userActionService.createOrUpdateGraphView(id, user);
    }
}
