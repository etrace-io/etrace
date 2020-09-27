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

package io.etrace.channel.kafka0822.impl.consumer;

import io.etrace.channel.kafka0822.impl.Client;
import io.etrace.channel.kafka0822.impl.MockServer;
import io.etrace.common.channel.KafkaConsumerProp;
import io.etrace.plugins.kafka0882.impl.impl.consumer.AbstractConsumer;
import kafka.message.MessageAndMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//todo
@Ignore("can't work right now")
public class KafkaConsumerTest {

    private MockServer mockServer;
    private Client client;
    private String topic = "test_topic";

    @Before
    public void init() {
        mockServer = new MockServer();
        mockServer.createTopic(topic);

        client = new Client(mockServer.getBrokerPort(), topic);
    }

    @Test
    public void testConsumer() {
        client.send();

        Map<String, String> consumerProps = new HashMap<>();
        consumerProps.put("zookeeper.connect", mockServer.getZkAddress());
        consumerProps.put("consumer.timeout.ms", "10000");
        consumerProps.put("auto.offset.reset", "smallest");

        KafkaConsumerProp kafkaConsumerProp = new KafkaConsumerProp();
        kafkaConsumerProp.setTopics(topic);
        kafkaConsumerProp.setGroup("log-cleaner-test-" + new Random().nextInt(Integer.MAX_VALUE));
        TracingConsumer consumer = new TracingConsumer(topic);
        consumer.startup(consumerProps, kafkaConsumerProp);

        consumer.destroy();
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

    private class TracingConsumer extends AbstractConsumer {

        TracingConsumer(String name) {
            super(name);
        }

        @Override
        public void onMessage(MessageAndMetadata<byte[], byte[]> msgAndMetadata) {
            System.out.println(new String(msgAndMetadata.key()) + " \t " + new String(msgAndMetadata.message()));
        }
    }

}
