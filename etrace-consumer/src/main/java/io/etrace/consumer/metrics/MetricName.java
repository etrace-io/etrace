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

package io.etrace.consumer.metrics;

public interface MetricName {
    /**
     * processor
     */
    String CALLSTACK_PARSE_ERROR = "callstack.parse.error";
    String CALLSTACK_CHECK_INVALID = "callstack.check.invalid";
    String HDFS_ERROR = "hdfs.error";
    String HDFS_THROUGHPUT = "hdfs.throughput";
    String METRIC_NO_SAMPLING = "metric.no.sampling";

    String TASK_DURATION = "task.duration";

    /**
     * sink
     */
    String HBASE_LATENCY = "hbase.put.latency";
    String HBASE_PUT = "hbase.put";
    String HBASE_FAIL = "hbase.fail";

    String CHECK_EXCEPTION = "check";
}
