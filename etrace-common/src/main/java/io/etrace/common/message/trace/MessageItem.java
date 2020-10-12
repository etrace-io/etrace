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

import io.etrace.common.util.RequestIdHelper;
import lombok.Data;

@Data
public class MessageItem {
    private CallStackV1 callStack;
    private long blockId;
    private int offset;
    private String dataFile;
    private String requestId;
    private byte[] messageData;

    private long hour;
    private long ip;
    private int index;

    private boolean isDal;

    public MessageItem(CallStackV1 callStack) {
        this(callStack, null);
    }

    public MessageItem(CallStackV1 callStack, MessageHeader messageHeader) {
        this.callStack = callStack;
        if (callStack != null) {
            this.requestId = RequestIdHelper.removeRootAppId(callStack.getRequestId());
        }
    }

    public String getSampleMessageId() {
        return this.requestId + "$$" + this.callStack.getId();
    }
}
