package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.DashboardAppPO;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.DashboardAppVO;
import io.etrace.api.service.DashboardAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/dashboard/app")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class DashboardAppController extends BaseController<DashboardAppVO, DashboardAppPO> {

    private final DashboardAppService dashboardAppService;

    @Autowired
    public DashboardAppController(DashboardAppService dashboardAppService) {
        super(dashboardAppService);
        this.dashboardAppService = dashboardAppService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("创建看板应用")
    public Long create(@RequestBody DashboardAppVO dashboardApp, @CurrentUser ETraceUser user) throws Exception {
        try {
            return doCreate(dashboardApp.toPO(), user);
        } catch (Exception e) {
            throw new BadRequestException("创建看板应用异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新看板应用")
    public void update(@RequestBody DashboardAppVO dashboardApp, @CurrentUser ETraceUser user) throws Exception {
        try {
            doUpdate(dashboardApp.toPO(), user);
        } catch (Exception e) {
            throw new BadRequestException("更新看板应用异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("看板应用查询")
    public ResponseEntity search(@RequestParam(value = "user", required = false) String user,
                                 @RequestParam(value = "departmentId", required = false) Long departmentId,
                                 @RequestParam(value = "productLineId", required = false) Long productLineId,
                                 @RequestParam(value = "title", required = false) String title,
                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                 @RequestParam(value = "critical", required = false) Boolean critical,
                                 @RequestParam(value = "current", defaultValue = "1") Integer pageNum,
                                 @CurrentUser ETraceUser eTraceUser)
        throws Exception {
        try {
            SearchResult<DashboardAppVO> result = dashboardAppService.search(title, departmentId, productLineId, user,
                pageNum, pageSize, critical);
            if (result.getTotal() > 0) {
                dashboardAppService.dashboardAppIsStar(eTraceUser, result.getResults());
            }
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            throw new BadRequestException("看板应用查询异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/business")
    @ApiOperation("关键应用配置查询")
    public ResponseEntity search(@RequestParam(value = "title", required = false) String title,
                                 @RequestParam(value = "critical", required = false) Boolean critical)
        throws Exception {
        try {
            List<DashboardAppVO> result = dashboardAppService.settingSearch(title, critical);
            if (result == null) {
                throw new Exception("关键应用配置查询异常");
            }
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            throw new BadRequestException("关键应用配置查询异常：" + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取某个看板应用的详细信息")
    public DashboardAppVO findById(@PathVariable("id") long id, @CurrentUser ETraceUser user) throws Exception {
        try {
            return DashboardAppVO.toVO(doFindById(id, user));
        } catch (Exception e) {
            throw new BadRequestException("获取看板应用的详细信息异常：" + e.getMessage());
        }
    }

    @Override
    protected void createOrUpdateView(long id, ETraceUser user) {

    }
}
