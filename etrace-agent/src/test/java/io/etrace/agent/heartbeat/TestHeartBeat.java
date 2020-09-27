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

package io.etrace.agent.heartbeat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Guice;
import io.etrace.agent.Trace;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.io.MessageSender;
import io.etrace.agent.module.InjectorFactory;
import io.etrace.agent.module.TestMessageSender;
import io.etrace.agent.module.TestModule;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.Message;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.util.JSONUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestHeartBeat {

    public static final long initialDelay = 1L;
    public static final long interval = 5L;

    @BeforeClass
    public static void initTestModule() {
        TestModule module = new TestModule();
        module.setHeartbeat(initialDelay, interval);
        InjectorFactory.setInjector(Guice.createInjector(module));

        AgentConfiguration.setAppId("test_appId");
    }

    @Test
    public void shouldOnlyOneRebootEvent() throws InterruptedException {
        TestMessageSender sender = (TestMessageSender)InjectorFactory.getInjector().getInstance(MessageSender.class);
        sender.clear();
        Trace.clean();

        // one heartbeat upload interval
        Thread.sleep(initialDelay * 1000 + 1500);

        sender.getQueue().forEach(
            data -> {
                //System.out.println("==== data: \n\t" + new String(data));

                List<CallStackV1> list = null;
                try {
                    list = JSONCodecV1.decodeListArrayFormat(data);
                } catch (IOException e) {
                    fail();
                }

                assertNotNull(list);
                assertEquals(1, list.size());
                //printCallstack(list.get(0));
            }
        );

        /*
        Reboot Event
        Environment Event
        System Status Transaction
        System Thread-Dump Transaction
         */
        assertEquals(4, sender.getQueueSize());

        sender.clear();
        Thread.sleep(interval * 1000);

        sender.getQueue().forEach(
            data -> {
                //System.out.println("==== data: \n\t" + new String(data));
                List<CallStackV1> list = null;
                try {
                    list = JSONCodecV1.decodeListArrayFormat(data);
                } catch (IOException e) {
                    fail();
                }

                assertNotNull(list);
                assertEquals(1, list.size());

                //printCallstack(list.get(0));
            }
        );

         /*
        System Status Transaction
        System Thread-Dump Transaction
         */
        assertEquals(2, sender.getQueueSize());
    }

    private void printCallstack(CallStackV1 callstack) {
        Message msg = callstack.getMessage();
        System.out.println(msg.getType() + "\t" + msg.getName() + "\t" + msg.getId() + "\t" + msg.getTimestamp() +
            "\t" + callstack.getRequestId());
        if (msg.getName().equals("Thread-Dump")) {
            try {
                System.out.println(JSONUtil.beautify(callstack));
            } catch (JsonProcessingException e) {
                fail();
            }
        }
    }
}
