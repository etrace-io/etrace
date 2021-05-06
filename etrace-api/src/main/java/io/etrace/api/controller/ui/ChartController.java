package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.model.vo.ui.Chart;
import io.etrace.api.service.ChartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/chart")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class ChartController {

    @Autowired
    private ChartService chartsService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("创建指标")
    public ResponseEntity save(@RequestBody Chart chart, @CurrentUser ETraceUser user) throws Exception {
        try {
            chart.setUpdatedBy(user.getUsername());
            chart.setCreatedBy(user.getUsername());
            Long id = chartsService.create(chart);
            return ResponseEntity.created(new URI("/chart/" + id)).body(id);
        } catch (Exception e) {
            throw new BadRequestException("新建指标异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新指标")
    public void update(@RequestBody Chart chart, @CurrentUser ETraceUser user) throws Exception {
        chartsService.update(chart, user);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("指标查询")
    public ResponseEntity search(@RequestParam(value = "title", required = false) String title,
                                 @RequestParam(value = "departmentId", required = false) Long departmentId,
                                 @RequestParam(value = "productLineId", required = false) Long productLineId,
                                 @RequestParam(value = "globalId", required = false) String globalId,
                                 @RequestParam(value = "user", required = false) String user,
                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                 @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                 @RequestParam(value = "status", defaultValue = "Active") String status)
        throws Exception {
        try {
            SearchResult<Chart> charts = chartsService.search(title, globalId, departmentId, productLineId, user,
                pageNum, pageSize, status);
            return ResponseEntity.ok().body(charts);
        } catch (Exception e) {
            throw new BadRequestException("指标查询异常：" + e.getMessage());
        }
    }

    @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取某个指标的详细信息")
    public ResponseEntity findChartById(@PathVariable("id") Long id) throws Exception {
        try {
            Optional<Chart> chartOp = chartsService.findChartById(id);
            if (chartOp.isPresent()) {
                return ResponseEntity.ok().body(chartOp.get());
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof UserForbiddenException) {
                throw new UserForbiddenException("获取某个指标的详细信息：" + e.getMessage());
            }

            throw new BadRequestException("获取某个指标的详细信息：" + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除某个指标")
    public void delete(@PathVariable("id") Long id,
                       @RequestParam(value = "status", defaultValue = "Inactive") String status,
                       @CurrentUser ETraceUser user) {
        Chart chart = new Chart();
        chart.setId(id);
        chart.setUpdatedBy(user.getUsername());
        chart.setStatus(status);
        chartsService.changeChartStatus(chart);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/sync")
    @ApiOperation("同步指标")
    public void syncChart(@RequestBody Chart chart, @CurrentUser ETraceUser user) throws Exception {
        chartsService.syncMetricConfig(chart, user);
    }

    @GetMapping("/checkGlobalId")
    @ApiOperation("校验globalId是否可用")
    public ResponseEntity chechGlobalIsValid(@RequestParam("globalId") String globalId) throws Exception {
        try {
            Chart chart = chartsService.findByGlobalId(globalId);
            if (null == chart) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.ok(false);
            }
        } catch (Exception e) {
            throw new BadRequestException("查询指标globalId是否可用异常：" + e.getMessage());
        }
    }
}
