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

package io.etrace.consumer.storage.hbase;

import io.etrace.common.message.metric.Metric;

import java.io.IOException;

public abstract class MetricTable implements TableSchema {

    /**
     * rowKey: shard(2) + metricType(1) + metricName + time(8) + (if timer and payload[maxValue(Long.MAX_VALUE -
     * maxValue)])  + tagHash(tagSize*8)
     */
    public abstract byte[] buildRowKey(short shard, Metric metric);

    /**
     *
     */
    public abstract byte[] buildQualifierValue(Metric metric) throws IOException;

    @Override
    public String getName() {
        return "metric";
    }

    @Override
    public byte[] getCf() {
        return "d".getBytes();
    }
}
