package io.etrace.api.controller.ui;

import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.HistoryLog;
import io.etrace.api.service.HistoryLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping(value = "/history")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class HistoryLogController {

    @Autowired
    private HistoryLogService historyLogService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/list")
    @ApiOperation("历史记录数据查询")
    public ResponseEntity queryHistoryLog(@RequestParam("type") String type, @RequestParam("id") Long id,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum)
        throws Exception {
        try {
            return ResponseEntity.ok(historyLogService.search(type, id, pageNum, pageSize));
        } catch (Exception e) {
            throw new BadRequestException("历史查询异常：" + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/detail")
    @ApiOperation("历史记录数据详情查询")
    public HistoryLog queryHistoryLogDetail(@RequestParam("type") String type, @RequestParam("id") Long id)
        throws Exception {
        try {
            Optional<HistoryLog> op = historyLogService.findExtendInfo(id);
            if (op.isPresent()) {
                return op.get();
            } else {
                throw new BadRequestException("历史记录详情查询异常，未找到历史记录！");
            }
        } catch (Exception e) {
            throw new BadRequestException("历史记录详情查询异常：" + e.getMessage());
        }
    }

}
