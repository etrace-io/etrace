package io.etrace.api.controller.misc;

import com.google.common.collect.Lists;
import io.etrace.api.controller.CurrentUser;
import io.etrace.api.model.po.misc.LimitSql;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.service.LimitSqlService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.etrace.api.config.SwaggerConfig.MISC;

@RestController
@RequestMapping(value = "/limitsql")
@Api(tags = MISC)
public class LimitSqlController {

    @Autowired
    private LimitSqlService limitSqlService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("添加黑名单sql")
    public LimitSql save(@RequestBody LimitSql limitSql, @CurrentUser ETraceUser user) {
        limitSql.setUpdatedBy(user.getUsername());
        limitSql.setCreatedBy(user.getUsername());
        return limitSqlService.create(limitSql);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新黑名单sql")
    public LimitSql update(@RequestBody LimitSql limitSql, @CurrentUser ETraceUser user) {
        limitSql.setUpdatedBy(user.getUsername());
        return limitSqlService.update(limitSql);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("黑名单sql查询")
    public List<LimitSql> search() {
        return Lists.newArrayList(limitSqlService.findAll());
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除黑名单sql")
    public void delete(@PathVariable("id") Long id,
                       @RequestParam(value = "status", defaultValue = "Inactive") String status,
                       @CurrentUser ETraceUser user) {
        limitSqlService.updateStatus(id, status, user.getUsername());
    }
}
