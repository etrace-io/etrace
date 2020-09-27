/*
 * Copyright 2019 etrace.io
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

package io.etrace.consumer.util;

import com.google.common.base.Strings;
import io.etrace.common.message.trace.CallStackV1;

public class CallStackUtil {

    public static boolean validate(CallStackV1 callStack) {
        if (callStack == null) {
            return false;
        } else if (Strings.isNullOrEmpty(callStack.getAppId())) {
            return false;
        } else if (Strings.isNullOrEmpty(callStack.getHostIp())) {
            return false;
        } else if (Strings.isNullOrEmpty(callStack.getHostName())) {
            return false;
        } else if (Strings.isNullOrEmpty(callStack.getRequestId())) {
            return false;
        } else if (Strings.isNullOrEmpty(callStack.getId())) {
            return false;
        } else {
            return callStack.getMessage() != null;
        }
    }

    public static void removeClientAppId(CallStackV1 callStack) {
        String id = callStack.getId();
        int index = id.indexOf("|");
        if (index >= 0) {
            callStack.setId(id.substring(index + 1));
        }
    }

}
