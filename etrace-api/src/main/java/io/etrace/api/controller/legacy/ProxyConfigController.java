package io.etrace.api.controller.legacy;

import io.etrace.api.model.po.misc.ProxyConfig;
import io.etrace.api.service.ProxyConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;
import static io.etrace.api.config.SwaggerConfig.PROXY_REQUEST_TAG;

@RestController
@RequestMapping(value = "/proxyConfig")
@Api(value = "proxyConfig", description = "代理配置相关API", tags = {PROXY_REQUEST_TAG, MYSQL_DATA})
public class ProxyConfigController {

    @Autowired
    private ProxyConfigService proxyConfigService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("新增代理配置")
    public Long create(@RequestBody ProxyConfig proxyConfig) {
        return proxyConfigService.create(proxyConfig);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新代理配置")
    public void update(@RequestBody ProxyConfig proxyConfig) {
        proxyConfigService.update(proxyConfig);
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除某个代理配置")
    public void delete(@PathVariable("id") Long id,
                       @RequestParam(value = "status", defaultValue = "Inactive") String status) {
        proxyConfigService.updateStatus(id, status);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("查询所有代理配置")
    public Iterable<ProxyConfig> findAllProxyConfig() {
        return proxyConfigService.findAll();
    }

}
