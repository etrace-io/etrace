package io.etrace.api.controller.ui;

import com.google.common.collect.Lists;
import io.etrace.api.model.po.user.UserRole;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.UserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/user_role")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class UserRoleController {
    @Autowired
    private UserRoleService userRoleService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("新增权限")
    public UserRole create(@RequestBody UserRole userRole) {
        return userRoleService.create(userRole);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新权限")
    public UserRole update(@RequestBody UserRole userRole) {
        return userRoleService.update(userRole);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/all")
    @ApiOperation("获取所有用户权限")
    public List<UserRole> findAllRoles() {
        return Lists.newArrayList(userRoleService.findAllRoles());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取用户权限信息")
    public SearchResult<UserRole> searchRoles(@RequestParam(value = "userEmail", required = false) String userEmail,
                                              @RequestParam(value = "userRole", required = false) Long userRole,
                                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                              @RequestParam(value = "current", defaultValue = "1") Integer pageNum) {
        SearchResult<UserRole> userRoles = userRoleService.findRoleByParam(userEmail, pageNum, pageSize);
        return userRoles;
    }
}
