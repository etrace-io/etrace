/*
 * Copyright 2020 etrace.io
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

package io.etrace.common.constant;

public interface InternalMetricName {
    String TASK_PENDING = "task.pending";
    String TASK_MSG_ERROR = "task.process.error";
    String TASK_QUEUE_REMAINING = "task.buffer.remaining";
    String TASK_PROCESS_DURATION = "task.process.duration";

    String EP_ENGINE_SEND_EVENT = "esper.send.event";
    String EP_ENGINE_UPDATE_EVENT = "esper.update.event";
    String KAFKA_CONSUME_THROUGHPUT = "kafka.consume";
    String KAFKA_CONSUME_ERROR = "kafka.consumer.error";
    String KAFKA_CONSUME_PENDING = "kafka.consumer.pending";
    String KAFKA_PRODUCER_METRIC_SEND = "kafka.producer.metric.send";
    String KAFKA_PRODUCER_THROUGHPUT = "kafka.produce";
    String KAFKA_PRODUCER_LOSE_DATA = "kafka.producer.lose.data";
    String KAFKA_BLOCK_STORE_SEND = "kafka.block.store.send";

    String LINDB_PRODUCER_SEND = "lindb.producer.send";

}
