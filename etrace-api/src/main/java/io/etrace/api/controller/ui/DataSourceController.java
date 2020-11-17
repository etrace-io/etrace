package io.etrace.api.controller.ui;

import com.google.common.collect.Lists;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.MetricDataSource;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.DataSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/datasource")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class DataSourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceController.class);

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("新增数据源")
    public ResponseEntity create(@RequestBody MetricDataSource metricDataSource) throws Exception {
        try {
            Long id = dataSourceService.create(metricDataSource);
            return ResponseEntity.created(new URI("/datasource/" + id)).body(id);
        } catch (Exception e) {
            throw new BadRequestException("新增数据源异常：" + e.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新数据源")
    public ResponseEntity update(@RequestBody MetricDataSource metricDataSource) throws Exception {
        try {
            dataSourceService.update(metricDataSource);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("更新数据源异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("查询数据源")
    public ResponseEntity search(@RequestParam(value = "type", required = false) String type,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                 @RequestParam(value = "pageNum", defaultValue = "1") int pageNum) throws Exception {
        try {
            SearchResult<MetricDataSource> searchResult = dataSourceService.search(type, name, status, pageSize,
                pageNum);
            if (searchResult != null) {
                return ResponseEntity.ok(searchResult);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("查询数据源异常：" + e.getMessage());
        }
    }

    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取所有数据源")
    public ResponseEntity findAll() throws Exception {
        try {
            List<MetricDataSource> metricDataSources = Lists.newArrayList(dataSourceService.findAll());
            if (metricDataSources != null) {
                return ResponseEntity.ok(metricDataSources);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("获取所有数据源异常：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("更新数据源状态")
    public ResponseEntity updateStatus(@PathVariable("id") long id, @RequestParam("status") String status)
        throws Exception {
        try {
            dataSourceService.updateStatus(id, status);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new BadRequestException("更新数据源状态异常：" + e.getMessage());
        }
    }

    @GetMapping("sync")
    @ApiOperation("更新所有数据源缓存")
    public ResponseEntity updateAllDataSourceConfig() throws Exception {
        // todo: use redis for cluster mode instand of by Huskar
        throw new RuntimeException("==updateAllDataSourceConfig== not implemented yet!");
    }
}
