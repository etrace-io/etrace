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

package io.etrace.common.message.trace;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class CallStackV1 {
    private String appId;
    private String hostIp;
    private String hostName;
    private String requestId;
    private String id;
    private Message message;
    private Map<String, String> extraProperties;

    public CallStackV1(String appId, String hostIp, String hostName, String requestId, String id,
                       Message message, Map<String, String> extraProperties) {
        this.appId = appId;
        this.hostIp = hostIp;
        this.hostName = hostName;
        this.requestId = requestId;
        this.id = id;
        this.message = message;
        this.extraProperties = extraProperties;
    }

    public void clear() {
        message = null;
        requestId = null;
        id = null;
        appId = null;
        hostIp = null;
        hostName = null;
        extraProperties = null;
    }
}
