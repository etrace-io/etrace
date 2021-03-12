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

package io.etrace.consumer.controller;

import io.etrace.common.message.trace.CallStackV1;
import io.etrace.consumer.exception.CallStackNotFoundException;
import io.etrace.consumer.service.CallStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/callstack")
public class CallStackController {

    @Autowired
    private CallStackService callStackService;

    @RequestMapping(method = RequestMethod.GET)
    public CallStackV1 queryCallStack(@RequestParam String messageId) throws Exception {
        CallStackV1 callStack = callStackService.queryByMessageId(messageId);
        if (null == callStack) {
            throw new CallStackNotFoundException("未能找到对应的记录requestId:".concat(messageId));
        }
        return callStack;
    }
}
