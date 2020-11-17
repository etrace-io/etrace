package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.UserActionService;
import io.etrace.api.service.graph.NodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import static com.google.common.collect.Lists.newArrayList;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/node")
@Api(tags = {MYSQL_DATA})
public class NodeController extends BaseController<Node> {

    private final NodeService nodeService;
    private final UserActionService userActionService;

    @Autowired
    public NodeController(NodeService nodeService, UserActionService userActionService) {
        super(nodeService);
        this.nodeService = nodeService;
        this.userActionService = userActionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("创建Node")
    public Long create(@RequestBody Node node, @CurrentUser ETraceUser user) throws Exception {
        try {
            return doCreate(node, user);
        } catch (Exception e) {
            throw new BadRequestException("创建Node异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Node查询")
    public SearchResult<Node> search(@RequestParam(value = "user", required = false) String user,
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
            throw new BadRequestException("Node查询异常：" + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取某个Node的详细信息")
    public Node findById(@PathVariable("id") long id, @CurrentUser ETraceUser user) throws Exception {
        try {
            return doFindById(id, user);
        } catch (Exception e) {
            throw new BadRequestException("获取Node的详细信息异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新Node")
    public void update(@RequestBody Node node, @CurrentUser ETraceUser user) throws Exception {
        try {
            doUpdate(node, user);
        } catch (Exception e) {
            throw new BadRequestException("更新Node异常：" + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/charts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新Node下的指标")
    public ResponseEntity updateChartIds(@PathVariable("id") long nodeId,
                                         @RequestBody Node node, @CurrentUser ETraceUser user) throws Exception {
        try {
            if (node == null) {
                node = new Node();
            }
            if (node.getChartIds() == null) {
                node.setChartIds(newArrayList());
            }
            node.setId(nodeId);
            node.setUpdatedBy(user.getUsername());
            nodeService.updateChartIds(node, user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("更新Node下的指标异常：" + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除Node")
    public void delete(@PathVariable("id") long id,
                       @RequestParam(value = "status", defaultValue = "Inactive") String status,
                       @CurrentUser ETraceUser user) throws Exception {
        try {
            doDelete(id, status, user);
        } catch (Exception e) {
            throw new BadRequestException("删除Node异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/sync")
    @ApiOperation("同步Node")
    public void syncNode(@RequestBody Node node, @CurrentUser ETraceUser user) throws Exception {
        nodeService.syncMetricConfig(node, user);
    }

    @PutMapping(value = "/query", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Node查询")
    public DeferredResult<ResponseEntity> queryNode(@RequestBody Node node, @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        try {
            if (node == null || node.getNodeType() == null || node.getCharts() == null
                || node.getCharts().size() <= 0) {
                result.setResult(ResponseEntity.badRequest().build());
            } else {
                try {
                    result.setResult(ResponseEntity.ok(nodeService.queryNode(node, user)));
                } catch (Throwable throwable) {
                    throw new BadRequestException("query node error:" + throwable.getMessage());
                }
            }
        } catch (Throwable throwable) {
            throw new BadRequestException("query node error:" + throwable.getMessage());
        }
        return result;
    }

    @Override
    protected void createOrUpdateView(long id, ETraceUser user) {
        userActionService.createOrUpdateNodeView(id, user);
    }
}
