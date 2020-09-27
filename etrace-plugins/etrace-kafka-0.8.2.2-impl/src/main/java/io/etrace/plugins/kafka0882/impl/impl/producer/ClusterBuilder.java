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

package io.etrace.plugins.kafka0882.impl.impl.producer;

import io.etrace.plugins.kafka0882.impl.impl.producer.sink.MetadataConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class ClusterBuilder {
    private String name;
    private Properties properties;
    private MetadataConfig metadataConfig = new MetadataConfig();

    public ClusterBuilder clusterName(String name) {
        this.name = name;
        return this;
    }

    public ClusterBuilder producerConfig(Properties prop) {
        this.properties = prop;
        return this;
    }

    public ClusterBuilder metadataConfig(MetadataConfig metadataConfig) {
        this.metadataConfig = metadataConfig;
        return this;
    }

    public KafkaCluster build() {
        if (null == name || name.length() < 1) {
            throw new IllegalArgumentException("handle size cannot be null");
        }
        if (null == properties) {
            throw new IllegalArgumentException("handle size cannot be null");
        }

        if (!properties.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArraySerializer");
        }
        if (!properties.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArraySerializer");
        }
        if (!properties.containsKey(ProducerConfig.MAX_REQUEST_SIZE_CONFIG)) {
            properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "10485760");
        }
        if (!properties.containsKey(ProducerConfig.RETRIES_CONFIG)) {
            properties.put(ProducerConfig.RETRIES_CONFIG, "10485760");
        }
        if (!properties.containsKey(ProducerConfig.SEND_BUFFER_CONFIG)) {
            properties.put(ProducerConfig.SEND_BUFFER_CONFIG, "2147483640");
        }
        if (!properties.containsKey(ProducerConfig.SEND_BUFFER_CONFIG)) {
            properties.put(ProducerConfig.SEND_BUFFER_CONFIG, "1048576");
        }
        return new KafkaCluster(this.name, this.properties, this.metadataConfig);
    }
}
