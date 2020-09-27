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

package io.etrace.agent.message.callstack;

import com.lmax.disruptor.EventFactory;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.Message;

import java.util.Map;

public class CallstackEvent {
    private CallStackV1 callStack = new CallStackV1();

    public CallStackV1 getCallStack() {
        return callStack;
    }

    public void reset(String appId, String hostIp, String hostName, String requestId, String messageId, Message message,
                      Map<String, String> extraProperties) {
        callStack.setAppId(appId);
        callStack.setHostIp(hostIp);
        callStack.setHostName(hostName);
        callStack.setRequestId(requestId);
        callStack.setId(messageId);
        callStack.setMessage(message);
        callStack.setExtraProperties(extraProperties);
    }

    public void clear() {
        this.callStack.clear();
    }

    public static class MessageEventFactory implements EventFactory<CallstackEvent> {
        @Override
        public CallstackEvent newInstance() {
            return new CallstackEvent();
        }
    }
}
