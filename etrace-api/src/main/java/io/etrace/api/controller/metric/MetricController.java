package io.etrace.api.controller.metric;

import com.google.common.base.Strings;
import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.service.MetricService;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.MetricDatasourceService;
import io.etrace.common.datasource.MetricQLBean;
import io.micrometer.core.instrument.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;

import static io.etrace.api.config.SwaggerConfig.METRIC;

@RestController
@RequestMapping(value = "/metric")
@Api(value = "DeferredResult", description = "数据查询相关api", tags = METRIC)
public class MetricController {

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricDatasourceService metricDatasourceService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/suggest")
    @ApiOperation("suggest数据查询")
    public DeferredResult<ResponseEntity> querySuggest(@RequestParam("ql") String ql,
                                                       @RequestParam("code") String monitorEntityCode,
                                                       @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        if (Strings.isNullOrEmpty(ql)) {
            result.setResult(ResponseEntity.badRequest().build());
        } else {
            try {
                result.setResult(
                    ResponseEntity.ok()
                        .body(metricService.querySuggest(new MetricQLBean(monitorEntityCode, ql), user)));
            } catch (Throwable throwable) {
                throw new BadRequestException("query suggest error:" + throwable.getMessage());
            }
        }
        return result;
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/tagSuggest")
    @ApiOperation("tagValues数据查询")
    public DeferredResult<ResponseEntity> querySuggestTag(@RequestBody MetricBean bean, @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        try {
            String ql = metricDatasourceService.generateSuggestQL(bean);
            if (!Strings.isNullOrEmpty(ql)) {
                result.setResult(
                    ResponseEntity.ok().body(metricService.querySuggest(new MetricQLBean(bean.getEntity(), ql), user)));
            } else {
                throw new BadRequestException("suggest ql build error");
            }

        } catch (Throwable throwable) {
            throw new BadRequestException("query suggest error:" + throwable.getMessage());
        }
        return result;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("数据查询")
    public DeferredResult<ResponseEntity> queryByQL(@RequestParam("ql") String ql,
                                                    @RequestParam("code") String monitorEntityCode,
                                                    @CurrentUser ETraceUser user) throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        if (StringUtils.isEmpty(ql)) {
            result.setResult(ResponseEntity.badRequest().build());
        } else {
            try {
                List<MetricQLBean> beans = new ArrayList<>(1);
                beans.add(new MetricQLBean(monitorEntityCode, ql));
                result.setResult(ResponseEntity.ok(metricService.queryDataWithMetricQLBean(beans, user)));
            } catch (Throwable throwable) {
                throw new BadRequestException("query metric error:" + throwable.getMessage());
            }
        }
        return result;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/single")
    @ApiOperation("单条数据查询(metric bean)")
    public DeferredResult<ResponseEntity> queryMetric(@RequestBody MetricBean bean, @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        try {
            List<MetricBean> beans = new ArrayList<>(1);
            beans.add(bean);
            result.setResult(ResponseEntity.ok(metricService.queryDataWithMetricBean(beans, user)));
        } catch (Throwable e) {
            throw new BadRequestException("query metric error:" + e.getMessage());
        }
        return result;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("多条数据查询(metric bean)")
    public DeferredResult<ResponseEntity> queryMetrics(@RequestBody List<MetricBean> qls, @CurrentUser ETraceUser user)
        throws Exception {
        DeferredResult<ResponseEntity> result = new DeferredResult<>(10000L);
        try {
            if (qls == null || qls.size() == 0) {
                result.setResult(ResponseEntity.badRequest().build());
            } else {
                try {
                    result.setResult(ResponseEntity.ok(metricService.queryDataWithMetricBean(qls, user)));
                } catch (Throwable throwable) {
                    throw new BadRequestException("query metrics error:" + throwable.getMessage());
                }
            }
        } catch (Throwable throwable) {
            throw new BadRequestException("query metrics error:" + throwable.getMessage());
        }
        return result;
    }

}

