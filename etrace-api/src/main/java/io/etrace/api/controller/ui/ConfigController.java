package io.etrace.api.controller.ui;

import io.etrace.api.model.po.misc.Config;
import io.etrace.api.service.ConfigService;
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
@RequestMapping(value = "/config")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("新建数据配置")
    public ResponseEntity create(@RequestBody Config config) {
        configService.create(config);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新数据配置")
    public ResponseEntity update(@RequestBody Config config) {
        configService.update(config);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取数据配置")
    public ResponseEntity queryAll(@RequestParam(value = "key", required = false) String key) {
        List<Config> configs = configService.findByKey(key);
        if (configs != null) {
            return ResponseEntity.ok(configs);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/{id}")
    @ApiOperation("删除数据配置")
    public ResponseEntity deleteConfig(@PathVariable("id") Long id,
                                       @RequestParam(value = "status", defaultValue = "Inactive") String status) {
        configService.deleteConfig(id, status);
        return ResponseEntity.noContent().build();
    }
}
