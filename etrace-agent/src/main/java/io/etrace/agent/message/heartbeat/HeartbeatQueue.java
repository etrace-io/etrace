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

package io.etrace.agent.message.heartbeat;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.io.MessageSender;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.Message;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.util.NetworkInterfaceHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HeartbeatQueue {
    @Inject
    private MessageSender messageSender;

    private String hostIp;
    private String hostName;

    public HeartbeatQueue() {
        hostIp = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
        hostName = NetworkInterfaceHelper.INSTANCE.getLocalHostName();
    }

    public boolean produce(String requestId, String rpcId, Message message) {
        ByteArrayOutputStream baos = null;
        try {
            CallStackV1 callStack = new CallStackV1(AgentConfiguration.getAppId(),
                hostIp, hostName, requestId, rpcId, message, AgentConfiguration.getExtraProperties());
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator generator = null;
            try {
                baos = new ByteArrayOutputStream();
                generator = jsonFactory.createGenerator(baos, JsonEncoding.UTF8);
                generator.writeStartArray();
                JSONCodecV1.encodeCallstackByArrayFormat(callStack, generator);
            } catch (IOException ignore) {
            } finally {
                if (generator != null) {
                    try {
                        generator.writeEndArray();
                        generator.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            messageSender.send(baos.toByteArray(), 1);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
