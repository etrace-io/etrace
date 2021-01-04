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

import com.google.common.collect.Maps;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.message.trace.impl.TransactionImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class V1JSONCodecTest {
    private static Map<String, String> extraProperties;

    @BeforeClass
    public static void init() {
        extraProperties = Maps.newHashMap();
        extraProperties.put("ezone", "ezone_value");
        extraProperties.put("cluster", "cluster_value");
        extraProperties.put("idc", "idc_value");

        extraProperties.put("instance", "instance_value");
        extraProperties.put("mesosTaskId", "taskId");
        extraProperties.put("eleapposLabel", "apposLable");
        extraProperties.put("eleapposSlaveFqdn", "apposFqdn");
    }

    @Test
    public void v1ToV1AndLegacy() throws IOException {
        Transaction transaction = new TransactionImpl("TestTransaction_type", "TestTransaction_ame");
        transaction.addTag("bbbbb", "ccccc");
        transaction.complete();

        CallStackV1 callStackV1Origin = buildCallStackV1();
        callStackV1Origin.setMessage(transaction);
        byte[] data = JSONCodecV1.encodeCallstackByArrayFormat(callStackV1Origin);

        CallStackV1 callstackV1 = JSONCodecV1.decodeToV1FromArrayFormat(data);
        assertEquals(callstackV1.getExtraProperties(), extraProperties);
    }

    private CallStackV1 buildCallStackV1() {
        CallStackV1 callStack = new CallStackV1();
        callStack.setRequestId("requestId");
        callStack.setId("id");
        callStack.setAppId("one_appId");
        callStack.setHostIp("127.0.0.1");
        callStack.setHostName("my_hostname");

        callStack.setExtraProperties(extraProperties);
        return callStack;
    }
}
