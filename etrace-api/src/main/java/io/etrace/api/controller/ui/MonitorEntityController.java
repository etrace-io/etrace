package io.etrace.api.controller.ui;

import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.MonitorEntity;
import io.etrace.api.service.MonitorEntityService;
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
@RequestMapping(value = "/entity")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class MonitorEntityController {
    @Autowired
    private MonitorEntityService monitorEntityService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("新增监控项")
    public ResponseEntity create(@RequestBody MonitorEntity entity) throws Exception {
        try {
            Long id = monitorEntityService.create(entity);
            if (id != null) {
                return ResponseEntity.ok().body(id);
            }
            return ResponseEntity.noContent().build();
        } catch (Throwable throwable) {
            throw new BadRequestException("新增监控项异常:" + throwable.getMessage());
        }
    }

    @GetMapping(value = "/{id}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取某个监控项下的子项")
    public ResponseEntity findByParentId(@PathVariable("id") long parentId,
                                         @RequestParam(value = "status", required = false) String status)
        throws Exception {
        try {
            List<MonitorEntity> monitorEntities = monitorEntityService.findByParentId(parentId, status);
            if (monitorEntities != null) {
                return ResponseEntity.ok().body(monitorEntities);
            }
            return ResponseEntity.noContent().build();
        } catch (Throwable throwable) {
            throw new BadRequestException("获取监控项下的子项异常:" + throwable.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("根据不同的type获取相应的监控项")
    public ResponseEntity findEntityByType(@RequestParam("type") String type,
                                           @RequestParam(value = "status", required = false) String status)
        throws Exception {
        try {
            List<MonitorEntity> monitorEntities = monitorEntityService.findEntityByType(type, status);
            if (monitorEntities != null) {
                return ResponseEntity.ok().body(monitorEntities);
            }
            return ResponseEntity.noContent().build();
        } catch (Throwable throwable) {
            throw new BadRequestException("根据不同的type获取相应的监控项异常:" + throwable.getMessage());
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("更新监控项")
    public ResponseEntity updateMonitor(@RequestBody MonitorEntity entity) throws Exception {
        try {
            monitorEntityService.update(entity);
            return ResponseEntity.noContent().build();
        } catch (Throwable throwable) {
            throw new BadRequestException("更新监控项异常:" + throwable.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("删除某个指标")
    public ResponseEntity delete(@PathVariable("id") Long id,
                                 @RequestParam(value = "status", defaultValue = "Inactive") String status)
        throws Exception {
        try {
            MonitorEntity monitorEntity = new MonitorEntity();
            monitorEntity.setId(id);
            monitorEntity.setStatus(status);
            monitorEntityService.changeStatus(monitorEntity);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {

            throw new BadRequestException("删除监控项异常：" + e.getMessage());
        }
    }
}
