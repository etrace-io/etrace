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

import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaCluster;
import io.etrace.plugins.kafka0882.impl.impl.producer.channel.Channel;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.AsyncCallback;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaSlaveProducer extends KafkaSink {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaSlaveProducer.class);
    private final int maxRetryCount = 2;
    private final int maxErrorCount = 5;
    private KafkaCluster kafkaCluster;
    private Channel channel;
    private volatile boolean running;
    private AtomicLong failureCount = new AtomicLong(0);
    /**
     * 上一次的静默时间
     */
    private volatile long lastSilentTime;
    private int checkInterval = 30 * 1000;
    private Thread consumer;

    public KafkaSlaveProducer(KafkaCluster kafkaCluster, String cluster, Properties prop, int brokerId,
                              Channel channel) {
        super(cluster, prop, brokerId);
        this.kafkaCluster = kafkaCluster;
        this.channel = channel;

    }

    public void start() {
        this.running = true;
        this.consumer = new Thread(new Sender());
        this.consumer.setName(this.name + "-producer-" + this.brokerId);
        this.consumer.start();
    }

    /**
     * 1.每条数据都会尝试maxRetryCount次数
     * <p>
     * 2.当errorCount超过maxErrorCount，静默checkInterval
     */
    public boolean send(RecordInfo message) throws Exception {
        for (int tryCount = 0; tryCount < maxRetryCount; ++tryCount) {
            if (failureCount.get() > maxErrorCount) {
                if ((System.currentTimeMillis() - lastSilentTime) > checkInterval) {
                    lastSilentTime = System.currentTimeMillis();
                } else {
                    return false;
                }
            }
            if (addToChannel(message)) {
                return true;
            }
        }
        failureCount.incrementAndGet();
        return false;
    }

    private boolean addToChannel(RecordInfo message) throws Exception {
        try {
            boolean success = channel.add(message);
            if (success) {
                failureCount.set(0);
                if (null != message.getCallback()) {
                    message.getCallback().before();
                }
                return true;
            }
        } catch (InterruptedException ignored) {
        }
        return false;
    }

    private void emit(RecordInfo record) {
        AsyncCallback callback = record.getCallback();
        if (null != callback) {
            callback.before();
        }
        try {
            producer.send(record.getRecord(), record.getCallback());
        } catch (Throwable e) {
            LOGGER.error("kafka cluster:[{}] brokerId:[{}] send throw a exception", this.name, this.brokerId, e);
            try {
                kafkaCluster.getNextPartition(record);
                kafkaCluster.getOrCreateProducer(record.getLeader()).send(record);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            long start = System.currentTimeMillis();
            while (channel.getSize() > 0) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ignored) {
                }
                if (System.currentTimeMillis() - start > 1000) {
                    LOGGER.error("kafka cluster:[{}] brokerId:[{}] close timeout:1s!", this.name, this.brokerId);
                    break;
                }
            }
        } finally {
            running = false;
            producer.close();
            this.consumer.interrupt();
        }
    }

    private class Sender implements Runnable {

        @Override
        public void run() {
            while (running) {
                try {
                    RecordInfo recordInfo = channel.take();
                    if (null != recordInfo) {
                        emit(recordInfo);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
