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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.etrace.consumer.metrics.MetricName.*;

@Service
public class MetricsService {

    private Cache<String, Counter> invalidCache;
    private Map<String, Timer> hBaseWriteTimer;
    private Map<String, Counter> hBaseWriteCounter;
    private Counter hbaseWriteErrorCounter;

    public MetricsService() {
        invalidCache = CacheBuilder.newBuilder().maximumSize(512).expireAfterAccess(5, TimeUnit.MINUTES).build();
        hBaseWriteCounter = new HashMap<>();
        hBaseWriteTimer = new HashMap<>();

        hbaseWriteErrorCounter = Metrics.counter(HBASE_FAIL, Tags.empty());
    }

    public void invalidCallStack(String type, String appId) {
        Counter counter = invalidCache.getIfPresent(type.concat("#").concat(appId));
        if (null == counter) {
            counter = Metrics.counter(CALLSTACK_CHECK_INVALID, Tags.of(Tag.of("type", type), Tag.of("agent", appId)));
            invalidCache.put(appId, counter);
        }
        counter.increment();
    }

    public void hBaseBatchPutCount(String tableName, int count) {
        Counter counter = hBaseWriteCounter.get(tableName);
        if (null == counter) {
            counter = Metrics.counter(HBASE_PUT, Tags.of("table", tableName));
            hBaseWriteCounter.put(tableName, counter);
        }
        counter.increment(count);
    }

    public void hBaseBatchDuration(String table, long duration) {
        Timer timer = hBaseWriteTimer.get(table);
        if (null == timer) {
            timer = Metrics.timer(HBASE_LATENCY, Tags.of("table", table));
            hBaseWriteTimer.put(table, timer);
        }
        timer.record(Duration.ofMillis(duration));
    }

    public void hbaseFail() {
        hbaseWriteErrorCounter.increment();
    }
}
