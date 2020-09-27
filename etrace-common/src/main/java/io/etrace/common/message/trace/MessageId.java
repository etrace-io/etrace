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

import com.google.common.base.Splitter;
import lombok.Getter;

import java.util.List;

@Getter
public class MessageId {

    private String requestId;
    private String rpcId;
    private long timestamp;

    public static MessageId parse(String messageId) {
        List<String> result = Splitter.on("$$").splitToList(messageId);
        if (result.size() < 2) {
            return null;
        }
        MessageId id = new MessageId();
        id.requestId = result.get(0);
        id.rpcId = result.get(1);
        result = Splitter.on("|").splitToList(id.requestId);
        if (result.size() >= 2) {
            id.timestamp = Long.valueOf(result.get(result.size() - 1));
        }
        return id;
    }
}
