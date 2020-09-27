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

package io.etrace.channel.kafka0822.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//@Ignore
public class KafkaTestIT {

    private MockServer mockServer;
    private String topic = "test_topic";

    private Client client;

    @Before
    public void before() {
        mockServer = new MockServer();
        mockServer.createTopic(topic);

        client = new Client(mockServer.getBrokerPort(), mockServer.getZkAddress(), topic);
    }

    @Test
    public void testProducerAndConsumer() {
        //consumer
        client.startConsumer();
        //producer
        client.send();
        client.close();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void after() throws InterruptedException {
        mockServer.shutdown();
    }

}
