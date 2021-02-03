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

package io.etrace.consumer.service;

import com.google.common.base.Strings;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.util.Pair;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import io.etrace.consumer.storage.hbase.IHBaseStorageService;
import io.etrace.consumer.storage.hbase.impl.MetricImpl;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HBaseBuildService {

    @Autowired
    private IHBaseClientFactory IHBaseClientFactory;
    @Autowired
    private MetricImpl metricImpl;
    @Autowired
    private IHBaseStorageService ihBaseStorageService;

    public Pair<Short, Put> buildMetricIndex(Metric metric) throws IOException {
        if (Strings.isNullOrEmpty(metric.getSampling())) {
            return null;
        }

        short shard = IHBaseClientFactory.getShardIdByLogicalTableName(metricImpl.getLogicalTableName(),
            metric.getTimestamp(),
            metricImpl.metricHashcode(metric.getMetricName()));
        byte[] samplingKey = metricImpl.buildRowKey(shard, metric);
        byte[] samplingValue = metricImpl.buildQualifierValue(metric);
        if (null == samplingKey || null == samplingValue) {
            return null;
        }

        Put put = ihBaseStorageService.createPut(samplingKey, metric.getTimestamp());
        put.addImmutable(metricImpl.getColumnFamily(), Bytes.toBytes(metric.getMetricType().code()), samplingValue);
        return new Pair<>(shard, put);
    }

}
