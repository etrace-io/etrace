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

package io.etrace.common;

import lombok.Data;

import java.util.Map;

// todo 很怪，写入时不是这样写入的
@Data
public class RequestIdInfo {

    String reqId;
    String rpcId;
    Long hour;
    String ip;
    Long index;
    Long blockId;
    Long offset;
    String rpcType;
    String appId;
    Map<String, Object> rpcInfo;

}
