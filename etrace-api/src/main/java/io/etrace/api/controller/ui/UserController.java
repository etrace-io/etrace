package io.etrace.api.controller.ui;

import com.google.common.collect.Sets;
import io.etrace.api.consts.RoleType;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.service.UserConfigService;
import io.etrace.api.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/user")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class UserController {

    private final UserService userService;

    private final UserConfigService userConfigService;

    @Autowired
    public UserController(UserService userService, UserConfigService userConfigService) {
        this.userService = userService;
        this.userConfigService = userConfigService;
    }

    @Deprecated
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "根据Token获取用户信息", response = ETraceUser.class)
    public ETraceUser getUserInfoByToken(@RequestParam(name = "token", required = false) String token)
        throws Exception {
        // todo: 以前是内部的校验模式，有安全风险；之后前端来改掉 /info 的验证逻辑
        ETraceUser tempUser = new ETraceUser();
        tempUser.setEmail("temp");
        tempUser.setUserName("Temp User");
        tempUser.setRoles(Sets.newHashSet(RoleType.USER.name()));
        return tempUser;
    }

    @Secured("ADMIN")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "根据keyword获取用户信息", response = ETraceUser.class)
    public List<ETraceUser> getUser(@RequestParam(value = "keyword") String keyword) {
        return userService.findByKeyword(keyword);
    }

    @Secured("ADMIN")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/all")
    @ApiOperation(value = "获取所有用户信息", response = ETraceUser.class)
    public List<ETraceUser> getAllUser(@RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "userRole", required = false) String userRole,
                                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                       @RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
        return userService.findAllUser(keyword, userRole, pageNum, pageSize);
    }
}
