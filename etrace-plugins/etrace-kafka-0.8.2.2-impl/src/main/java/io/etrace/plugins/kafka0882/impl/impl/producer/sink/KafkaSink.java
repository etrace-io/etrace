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

package io.etrace.plugins.kafka0882.impl.impl.producer.sink;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.Closeable;
import java.util.Properties;

public abstract class KafkaSink implements Closeable {
    final int brokerId;
    final String name;
    KafkaProducer producer;

    KafkaSink(String cluster, Properties prop, int brokerId) {
        this.name = cluster + "-Broker-" + brokerId;
        this.brokerId = brokerId;
        this.producer = new KafkaProducer(prop);
    }
}
