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

package io.etrace.common.message.trace;

import lombok.Data;

@Data
public class MessageHeader {

    /**
     * 开源版本的第一个版本。开始添加该version字段
     */
    public static final String V1_0 = "v1.0";

    /**
     * reserved. used to set partition routing value in origin etrace version.
     */
    @Deprecated
    private String key;

    private String version;

    private String tenant;

    private String appId;
    private String hostIp;
    private String hostName;
    private String instance;

    private long ast;//agent send time
    private long crt;//collector receive time
    private long cst;//collector send time

    private long csrt;//consumer receive time
    /**
     * todo: 消息类型 ??
     */
    private String messageType;
}
