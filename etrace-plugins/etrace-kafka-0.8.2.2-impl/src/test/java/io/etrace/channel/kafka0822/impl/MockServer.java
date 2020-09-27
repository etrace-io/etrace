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

import kafka.admin.TopicCommand;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.zk.EmbeddedZookeeper;
import lombok.Data;
import org.I0Itec.zkclient.ZkClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Data
public class MockServer {
    private String zkAddress;
    private int brokerPort;
    private int brokers;

    private ZkClient zkClient;
    private EmbeddedZookeeper zkServer;
    private List<KafkaServer> servers;

    public MockServer() {
        this("127.0.0.1:2181", 9092, 4);
    }

    public MockServer(String zkAddress, int brokerPort, int brokers) {
        this.zkAddress = zkAddress;
        this.brokerPort = brokerPort;
        this.brokers = brokers;

        zkServer = new EmbeddedZookeeper(zkAddress);
        zkClient = new ZkClient(zkServer.connectString(), 30000, 30000, ZKStringSerializer$.MODULE$);
        servers = startKafkaServers();
    }

    private List<KafkaServer> startKafkaServers() {
        List<KafkaServer> servers = new ArrayList<>();
        for (int i = 0; i < brokers; i++) {
            Properties props = TestUtils.createBrokerConfig(i, brokerPort + i, true);
            props.put("zookeeper.connect", zkAddress);
            KafkaConfig config = new KafkaConfig(props);
            KafkaServer kafkaServer = TestUtils.createServer(config, new MockTime());
            servers.add(kafkaServer);
        }
        return servers;
    }

    public void createTopic(String topic) {
        String[] arguments = new String[] {"--topic", topic, "--partitions", "6", "--replication-factor", "1"};
        // create topic
        TopicCommand.createTopic(zkClient, new TopicCommand.TopicCommandOptions(arguments));

        TestUtils.waitUntilMetadataIsPropagated(scala.collection.JavaConversions.asScalaBuffer(servers), topic, 0,
            5000);
    }

    public void shutdown() throws InterruptedException {
        for (KafkaServer server : servers) {
            server.shutdown();
        }
        zkClient.close();
        zkServer.shutdown();
        Thread.sleep(2000);
    }

}
