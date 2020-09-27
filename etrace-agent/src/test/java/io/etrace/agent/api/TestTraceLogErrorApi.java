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

package io.etrace.agent.api;

import com.google.inject.Guice;
import io.etrace.agent.Trace;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.io.MessageSender;
import io.etrace.agent.message.callstack.CallstackQueue;
import io.etrace.agent.module.InjectorFactory;
import io.etrace.agent.module.TestMessageSender;
import io.etrace.agent.module.TestModule;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.Event;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TestTraceLogErrorApi {

    @BeforeClass
    public static void initTestModule() {
        InjectorFactory.setInjector(Guice.createInjector(new TestModule()));

        AgentConfiguration.setAppId("test_appId");
    }

    @Test
    public void logError() throws InterruptedException, IOException {
        TestMessageSender sender = (TestMessageSender)InjectorFactory.getInjector().getInstance(MessageSender.class);
        sender.clear();

        Trace.logError(new RuntimeException());

        Thread.sleep(CallstackQueue.PULL_INTERVAL_IN_MILLISECOND + 10);

        assertEquals("queue size =1", 1, sender.getQueueSize());
        assertEquals("queue message = 1", 1, sender.getMessageCount());

        byte[] data = sender.getQueue().get(0);
        //System.out.println(new String(data));

        List<CallStackV1> list = JSONCodecV1.decodeListArrayFormat(data);
        assertNotNull(list);
        assertEquals("list size = 1", 1, list.size());
        CallStackV1 callstack = list.get(0);
        assertEquals("test_appId", callstack.getAppId());
        assertTrue(callstack.getMessage() instanceof Event);

        Event event = (Event)callstack.getMessage();
        assertEquals("RuntimeException", event.getType());
        assertEquals("java.lang.RuntimeException", event.getName());
        assertEquals("ERROR", event.getStatus());
        assertEquals("event requestId =1 ", 1L, event.getId());
    }
}
