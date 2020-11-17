//package io.etrace.api.controller.metric;
//
//import com.google.common.base.Strings;
//import io.etrace.api.exception.BadRequestException;
//import io.etrace.api.service.MetricService;
//import io.etrace.common.util.JSONUtil;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static io.etrace.api.config.SwaggerConfig.METRIC;
//
//@RestController
//@RequestMapping(value = "/api")
//@Api(value = "/api", description = "外部调用api", tags = METRIC)
//public class ApiController {
//
//    @Autowired
//    private MetricService metricService;
//
//    @GetMapping(value = {"/query/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiOperation("数据查询")
//    public ResponseEntity queryByQL(@RequestParam("ql") String ql, @PathVariable("code") String monitorEntityCode)
//    throws Exception {
//        if (Strings.isNullOrEmpty(ql)) {
//            return ResponseEntity.badRequest().build();
//        } else {
//            try {
//                QLResult qlResult = metricService.queryDataForLinDB2(monitorEntityCode, ql);
//                return ResponseEntity.ok().body(qlResult);
//            } catch (Throwable throwable) {
//                throw new BadRequestException("query metric error:" + throwable.getMessage());
//            }
//        }
//    }
//
//    @GetMapping(value = {"/suggest/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiOperation("元数据查询")
//    public ResponseEntity searchForLinDB2(@PathVariable("code") String db,
//                                          @RequestParam(name = "prefix", required = false) String prefix,
//                                          @RequestParam(name = "type", required = false) String type,
//                                          @RequestParam(name = "metricName", required = false) String metricName,
//                                          @RequestParam(name = "tagName", required = false) String tagName) throws
//                                          Throwable {
//        Map<String, String> params = new HashMap<>();
//        if (!Strings.isNullOrEmpty(prefix)) {
//            params.put("prefix", prefix);
//        }
//        if (!Strings.isNullOrEmpty(type)) {
//            params.put("type", type);
//        }
//        if (!Strings.isNullOrEmpty(metricName)) {
//            params.put("metricName", metricName);
//        }
//        if (!Strings.isNullOrEmpty(tagName)) {
//            params.put("tagName", tagName);
//        }
//        return ResponseEntity.ok(JSONUtil.toString(metricService.queryMetaForLinDB2(db, params)));
//    }
//}
