package io.etrace.api.controller.meta;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.service.AppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.etrace.api.config.SwaggerConfig.META;

@RestController
@RequestMapping()
@Api(tags = {META})
public class AppIdController {
    private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(AppIdController.class);

    @Autowired
    private AppService appService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = {"/app/appId", "/api/v1/app/appId"})
    @ApiOperation("根据appid key查询appid")
    public ResponseEntity getAppIdByAhead(@RequestParam("appId") String appId,
                                          @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        List<String> apps = Lists.newLinkedList();
        try {
            Set<String> appIdSet = appService.findAppIdFromMysqlAndPgCache(URLDecoder.decode(appId, "UTF-8"));
            if (!CollectionUtils.isEmpty(appIdSet)) {
                apps =
                    appIdSet.stream().limit(pageSize).filter(r -> !Strings.isNullOrEmpty(r)).collect(
                        Collectors.toList());
            } else {
                apps = new ArrayList<>(appIdSet);
            }
        } catch (UnsupportedEncodingException e) {
            Logger.error("The Character Encoding utf-8 is not supported,this should not exists!!!", e);
        }
        return ResponseEntity.ok(apps);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = {"/app/detail", "/api/v1/app/detail"})
    @ApiOperation("根据appid 查询应用的部门信息等。其中返回结果中返回两级部门信息，departmentName为一级部门，productLineName为二级部门")
    public ResponseEntity getAppInfo(@RequestParam("appId") String appId) throws BadRequestException {
        try {
            return ResponseEntity.ok(appService.findByAppId(appId));
        } catch (Exception e) {
            throw new BadRequestException("appid信息查询异常：" + e.getMessage());
        }
    }
}
