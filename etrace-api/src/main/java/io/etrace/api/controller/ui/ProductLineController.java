package io.etrace.api.controller.ui;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static io.etrace.api.config.SwaggerConfig.DEPRECATED_TAG;

@RestController
@RequestMapping(value = "/productline")
@Api(tags = {DEPRECATED_TAG})
public class ProductLineController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取所有产品线")
    public List findAll() throws Exception {
        return Collections.emptyList();
    }

    @GetMapping(value = "/{departmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "根据departmentId获取相应的产品线")
    public List findById(@PathVariable("departmentId") Long departmentId) throws Exception {
        return Collections.emptyList();
    }

    @GetMapping(value = "/{departmentId}/used", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "根据departmentId获取相应的产品线")
    public List findByIdAndType(@PathVariable("departmentId") Long departmentId,
                                @RequestParam(value = "type", defaultValue = "Dashboard") String type,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "user", required = false) String user,
                                @RequestParam(value = "favorite", defaultValue = "false") Boolean favorite) {
        return Collections.emptyList();
    }
}
