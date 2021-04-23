package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserConfigPO;
import io.etrace.api.service.UserConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/user-config")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class UserConfigController {
    @Autowired
    private UserConfigService userConfigService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("创建用户配置")
    public UserConfigPO save(@RequestBody UserConfigPO userConfig, @CurrentUser ETraceUser user) {
        return userConfigService.createOrUpdate(userConfig, user);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户配置查询")
    public UserConfigPO search(@CurrentUser ETraceUser user) throws Exception {
        UserConfigPO userConfig = userConfigService.findUserConfig(user);
        if (userConfig != null) {
            return userConfig;
        } else {
            throw new BadRequestException("用户配置查询异常：");
        }
    }
}
