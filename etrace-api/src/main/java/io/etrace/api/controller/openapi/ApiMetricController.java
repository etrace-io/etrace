package io.etrace.api.controller.openapi;

import com.google.common.base.Strings;
import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.metric.MetricDatasourceManager;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.service.MetricService;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.MetricQLBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;

import static io.etrace.api.config.SwaggerConfig.OPEN_API_TAG;

@RestController
@RequestMapping(value = "/api")
@Api(value = "/api", tags = OPEN_API_TAG)
public class ApiMetricController {

    @Autowired
    private MetricService metricService;
    @Autowired
    private MetricDatasourceManager metricDatasourceManager;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/v1/metric")
    @ApiOperation("v1版本数据查询")
    public DeferredResult<ResponseEntity> queryByQL(@RequestParam("ql") String ql, @RequestParam("code") String code,
                                                    @CurrentUser
                                                        ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        if (Strings.isNullOrEmpty(ql) || Strings.isNullOrEmpty(code)) {
            result.setResult(ResponseEntity.badRequest().build());
        } else {
            try {
                List<MetricQLBean> beans = new ArrayList<>(1);
                beans.add(new MetricQLBean(code, ql));
                result.setResult(ResponseEntity.ok(metricService.queryDataWithMetricQLBean(beans, user)));
            } catch (Throwable throwable) {
                throw new BadRequestException("api query metric error:" + throwable.getMessage());
            }
        }
        return result;
    }

    @PutMapping(value = "/v1/metric", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("多条数据查询(metric bean)")
    public DeferredResult<ResponseEntity> queryMetrics(@RequestBody List<MetricBean> qls, @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        if (CollectionUtils.isEmpty(qls)) {
            result.setResult(ResponseEntity.badRequest().build());
        }
        try {
            result.setResult(ResponseEntity.ok(metricService.queryDataWithMetricBean(qls, user)));
        } catch (Throwable throwable) {
            throw new BadRequestException("api query metrics error:" + throwable.getMessage());
        }
        return result;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/v1/metric/suggest")
    @ApiOperation("suggest数据查询")
    public DeferredResult<ResponseEntity> querySuggest(@RequestParam("ql") String ql,
                                                       @RequestParam("code") String monitorEntityCode,
                                                       @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result;
        result = new DeferredResult<>(10000L);
        if (Strings.isNullOrEmpty(ql)) {
            result.setResult(ResponseEntity.badRequest().build());
        } else {
            try {
                result.setResult(
                    ResponseEntity.ok()
                        .body(metricService.querySuggest(new MetricQLBean(monitorEntityCode, ql), user)));
            } catch (Throwable throwable) {
                throw new BadRequestException("api query suggest error:" + throwable.getMessage());
            }
        }
        return result;
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/v1/metric/tagSuggest")
    @ApiOperation("tagValues数据查询")
    public DeferredResult<ResponseEntity> querySuggestTag(@RequestBody MetricBean bean, @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        try {
            String ql = metricDatasourceManager.generateSuggestQL(bean);
            if (Strings.isNullOrEmpty(ql)) {
                result.setResult(
                    ResponseEntity.ok().body(metricService.querySuggest(new MetricQLBean(bean.getEntity(), ql), user)));
            } else {
                throw new BadRequestException("api suggest ql build error");
            }

        } catch (Throwable throwable) {
            throw new BadRequestException("api query suggest error:" + throwable.getMessage());
        }
        return result;
    }
}
