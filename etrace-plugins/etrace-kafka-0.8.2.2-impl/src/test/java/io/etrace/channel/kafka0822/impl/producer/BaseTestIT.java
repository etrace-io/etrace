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

package io.etrace.channel.kafka0822.impl.producer;

import io.etrace.plugins.kafka0882.impl.impl.producer.ClusterBuilder;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaCluster;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaEmitter;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaManager;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import kafka.admin.TopicCommand;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.VerifiableProperties;
import kafka.utils.ZKStringSerializer$;
import kafka.zk.EmbeddedZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseTestIT {

    private int startKafkaServer(String topic, int startPort, String zkAddress) {
        // start Zookeeper
        EmbeddedZookeeper zkServer = new EmbeddedZookeeper(zkAddress);
        ZkClient zkClient = new ZkClient(zkServer.connectString(), 30000, 30000, ZKStringSerializer$.MODULE$);
        int port = zkServer.zookeeper().getClientPort();

        //start Broker
        List<KafkaServer> servers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Properties props = TestUtils.createBrokerConfig(i, startPort + i, true);
            props.put("zookeeper.connect", zkAddress);
            KafkaConfig config = new KafkaConfig(props);
            KafkaServer kafkaServer = TestUtils.createServer(config, new MockTime());
            servers.add(kafkaServer);
        }

        String[] arguments = new String[] {"--topic", topic, "--partitions", "6", "--replication-factor", "1"};
        // create topic
        TopicCommand.createTopic(zkClient, new TopicCommand.TopicCommandOptions(arguments));

        TestUtils.waitUntilMetadataIsPropagated(scala.collection.JavaConversions.asScalaBuffer(servers), topic, 0,
            5000);
        return port;
    }

    private Properties getProp(int startPort) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers",
            "127.0.0.1:" + startPort + ",127.0.0.1:" + (startPort + 1) + ",127.0.0.1:" + (startPort + 2));
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("max.request.size", "10485760");
        properties.put("retries", "2147483640");
        return properties;
    }

    private void startConsumer(String topic, int zkPort) {
        ConsumerConnector appConsumer = makeConsumer(zkPort);
        ExecutorService executor = Executors.newCachedThreadPool();

        TopicFilter topicFilter = new Whitelist(topic);
        List<KafkaStream<byte[], byte[]>> streams = appConsumer.createMessageStreamsByFilter(topicFilter, 4);
        for (KafkaStream<byte[], byte[]> stream : streams) {
            executor.execute(() -> {
                for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                    StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
                    System.out.println(msgAndMetadata.topic() + " " + msgAndMetadata.partition() + " " + valueDecoder
                        .fromBytes(msgAndMetadata.message()));
                }
            });
        }

        //        Map<String, Integer> topicMap = new HashMap<>();
        //        topicMap.put(topic, 1);
        //        Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams = appConsumer
        //        .createMessageStreams(topicMap);
        //        for (List<KafkaStream<byte[], byte[]>> kafkaStreams : topicMessageStreams.values()) {
        //            for (KafkaStream<byte[], byte[]> stream : kafkaStreams) {
        //                executor.execute(() -> {
        //                    for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
        //                        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
        //                        System.out.println(msgAndMetadata.topic() + " " +msgAndMetadata.partition() + " " +
        //                        valueDecoder.fromBytes(msgAndMetadata.message()));
        //                    }
        //                });
        //            }
        //    }

    }

    private ConsumerConnector makeConsumer(int zkPort) {
        Properties consumerProps = new Properties();
        consumerProps.setProperty("group.id", "log-cleaner-test-" + new Random().nextInt(Integer.MAX_VALUE));
        consumerProps.setProperty("zookeeper.connect", "127.0.0.1:" + zkPort);
        consumerProps.setProperty("consumer.timeout.ms", "10000");
        consumerProps.setProperty("auto.offset.reset", "smallest");
        return Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProps));
    }

    @Test
    public void test() throws Exception {
        String appTopic = "app";
        int appPort = 9092;
        int appZK = startKafkaServer(appTopic, appPort, "127.0.0.1:54844");
        KafkaCluster kafkaCluster = new ClusterBuilder().clusterName("application")
            .producerConfig(getProp(appPort)).build();
        KafkaManager.getInstance().register(appTopic, kafkaCluster);

        startConsumer(appTopic, appZK);

        String dalTopic = "dal";
        int dalPort = 8082;
        int dalZK = startKafkaServer(dalTopic, dalPort, "127.0.0.1:54845");
        kafkaCluster = new ClusterBuilder().clusterName("dal")
            .producerConfig(getProp(dalPort)).build();
        KafkaManager.getInstance().register(dalTopic, kafkaCluster);

        startConsumer(dalTopic, dalZK);

        for (int i = 0; i < 100; i++) {
            if ((i & 1) == 0) {
                KafkaEmitter.randomSend(new RecordInfo(new ProducerRecord(appTopic, "value" + i), null));
            } else {
                KafkaEmitter.randomSend(new RecordInfo(new ProducerRecord(dalTopic, "value" + i), null));
            }
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
