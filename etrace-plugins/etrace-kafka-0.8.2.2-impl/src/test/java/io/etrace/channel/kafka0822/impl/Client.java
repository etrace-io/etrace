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

import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private int brokerPort;
    private String zkAddress;
    private String topic;
    private ConsumerConnector consumerConnector;
    private volatile boolean running = true;

    public Client() {
    }

    public Client(int brokerPort, String topic) {
        this.brokerPort = brokerPort;
        this.topic = topic;
    }

    public Client(String zkAddress, String topic) {
        this(-1, zkAddress, topic);
    }

    public Client(int brokerPort, String zkAddress, String topic) {
        this.brokerPort = brokerPort;
        this.zkAddress = zkAddress;
        this.topic = topic;
        this.consumerConnector = createConsumer();
    }

    private ConsumerConnector createConsumer() {
        Properties consumerProps = new Properties();
        consumerProps.setProperty("group.id", "log-cleaner-test-" + new Random().nextInt(Integer.MAX_VALUE));
        consumerProps.setProperty("zookeeper.connect", zkAddress);
        consumerProps.setProperty("consumer.timeout.ms", "10000");
        consumerProps.setProperty("auto.offset.reset", "smallest");
        return kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProps));
    }

    public void startConsumer() {
        ExecutorService executor = Executors.newCachedThreadPool();

        TopicFilter topicFilter = new Whitelist(topic);
        List<KafkaStream<byte[], byte[]>> streams = consumerConnector.createMessageStreamsByFilter(topicFilter, 4);
        for (KafkaStream<byte[], byte[]> stream : streams) {
            executor.execute(() -> {
                ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
                while (running) {
                    MessageAndMetadata<byte[], byte[]> msgAndMetadata = iterator.next();
                    StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
                    System.out.println(
                        msgAndMetadata.topic() + " " + valueDecoder.fromBytes(msgAndMetadata.key()) + " " + valueDecoder
                            .fromBytes(msgAndMetadata.message()));
                }
            });
        }
    }

    public void send() {
        KafkaProducer<String, String> producer = new KafkaProducer<>(getProp(brokerPort));
        for (int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<>(topic, "key_" + i, UUID.randomUUID().toString()));
        }
        producer.close();
    }

    public void close() {
        running = false;
        if (null != consumerConnector) {
            consumerConnector.commitOffsets(true);
            consumerConnector.shutdown();
        }
    }

    private Properties getProp(int startPort) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "127.0.0.1:" + startPort);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("max.request.size", "10485760");
        properties.put("retries", "2147483640");
        return properties;
    }
}
