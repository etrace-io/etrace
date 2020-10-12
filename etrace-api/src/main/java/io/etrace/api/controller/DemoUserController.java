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

package io.etrace.api.controller;

import io.etrace.api.model.po.user.ETraceUser;
import io.swagger.annotations.Api;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.etrace.api.config.SwaggerConfig.DEPRECATED_TAG;

@Api(value = "Test", description = "only for test", tags = {DEPRECATED_TAG})
@RestController
public class DemoUserController {

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping("/test-user")
    public String getUser(@CurrentUser ETraceUser user) {
        Authentication au = SecurityContextHolder.getContext().getAuthentication();
        return user.toString() + "\n" + SecurityContextHolder.getContext().getAuthentication();
    }
}
