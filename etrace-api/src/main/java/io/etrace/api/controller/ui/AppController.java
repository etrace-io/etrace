/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.api.controller.ui;

import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.App;
import io.etrace.api.service.AppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// todo?  属于哪里的
@RestController
@RequestMapping(value = "/app")
@Api(value = "App", description = "App相关API", tags = {})
public class AppController {

    @Autowired
    private AppService appService;

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("指标查询")
    public List<App> search(@RequestParam(value = "appId", required = false) String appId,
                            @RequestParam(value = "critical", required = false) Boolean critical) throws Exception {
        try {
            return appService.findLikeAppId(appId, critical);
        } catch (Exception e) {
            throw new BadRequestException("指标查询异常：" + e.getMessage());
        }
    }
}
