package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.DashboardPO;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.service.DashboardService;
import io.etrace.api.service.UserActionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.google.common.collect.Lists.newArrayList;
import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/dashboard")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class DashboardController extends BaseController<DashboardVO, DashboardPO> {

    private final DashboardService dashboardService;
    private final UserActionService userActionService;

    @Autowired
    public DashboardController(DashboardService dashboardService,
                               UserActionService userActionService) {
        super(dashboardService);
        this.dashboardService = dashboardService;
        this.userActionService = userActionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("创建看板")
    public Long create(@RequestBody DashboardVO dashboard, @CurrentUser ETraceUser user) throws Exception {
        try {
            return doCreate(dashboard.toPO(), user);
        } catch (Exception e) {
            throw new BadRequestException("创建看板异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("看板查询")
    public SearchResult<DashboardVO> search(@RequestParam(value = "user", required = false) String user,
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
            throw new BadRequestException("看板查询异常：" + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取某个看板的详细信息")
    public DashboardVO findById(@PathVariable("id") long id, @CurrentUser ETraceUser user) throws Exception {
        try {
            return DashboardVO.toVO(doFindById(id, user));
        } catch (Exception e) {
            throw new BadRequestException("获取看板的详细信息异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新看板")
    public void updateDashboard(@RequestBody DashboardVO dashboard, @CurrentUser ETraceUser user) throws Exception {
        try {
            doUpdate(dashboard.toPO(), user);
        } catch (Exception e) {
            throw new BadRequestException("更新看板异常：" + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/charts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新看板下的指标")
    public ResponseEntity updateDashboardChartIds(@PathVariable("id") long dashboardId,
                                                  @RequestBody DashboardVO dashboard, @CurrentUser ETraceUser user)
        throws Exception {
        try {
            if (dashboard == null) {
                dashboard = new DashboardVO();
            }
            if (dashboard.getChartIds() == null) {
                dashboard.setChartIds(newArrayList());
            }
            dashboard.setId(dashboardId);
            dashboard.setUpdatedBy(user.getUsername());
            dashboardService.updateDashboardChartIds(dashboard, user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("更新看板下的指标异常：" + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除看板")
    public void deleteDashboard(@PathVariable("id") long id,
                                @RequestParam(value = "status", defaultValue = "Inactive") String status,
                                @CurrentUser ETraceUser user)
        throws Exception {
        try {
            doDelete(id, status, user);
        } catch (Exception e) {
            throw new BadRequestException("删除看板异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/sync")
    @ApiOperation("同步看板")
    public void syncDashboard(@RequestBody DashboardVO dashboard, @CurrentUser ETraceUser user) throws Exception {
        dashboardService.syncMetricConfig(dashboard, user);
    }

    @GetMapping("/checkGlobalId")
    @ApiOperation("校验globalId是否可用")
    public ResponseEntity chechGlobalIsValid(@RequestParam("globalId") String globalId) throws Exception {
        try {
            DashboardVO dashboard = dashboardService.findByGlobalId(globalId);
            if (null == dashboard) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.ok(false);
            }
        } catch (Exception e) {
            throw new BadRequestException("查询看板globalid是否可用异常：" + e.getMessage());
        }
    }

    @Override
    protected void createOrUpdateView(long id, ETraceUser user) {
        userActionService.createOrUpdateView(id, user);
    }
}
