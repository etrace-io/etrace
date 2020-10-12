package io.etrace.api.controller.ui;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static io.etrace.api.config.SwaggerConfig.*;

@RestController
@RequestMapping(value = "/department")
@Api(tags = {FOR_ETRACE, MYSQL_DATA, DEPRECATED_TAG})
public class DepartmentController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取所有部门数据")
    public List findAll() throws Exception {
        return Collections.emptyList();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/used")
    @ApiOperation(value = "获取所有部门存在面板、看板应用、指标的数据")
    public List findAllByUsed(@RequestParam("type") String type,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "user", required = false) String user,
                              @RequestParam(value = "favorite", defaultValue = "false") Boolean favorite)
        throws Exception {
        return Collections.emptyList();
    }

    @GetMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取组织信息")
    public List findAllDepartment() throws Exception {
        return Collections.emptyList();

    }

    @GetMapping(value = "/detail/used", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取组织信息")
    public List findAllDepartmentByType(
        @RequestParam(value = "type", defaultValue = "Dashboard") String type)
        throws Exception {
        return Collections.emptyList();
    }
}
