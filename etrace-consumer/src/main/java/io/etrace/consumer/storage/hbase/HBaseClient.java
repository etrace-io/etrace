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

import io.etrace.consumer.metrics.MetricsService;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class HBaseClient {

    @Autowired
    private HBaseClientFactory hBaseClientFactory;
    @Autowired
    private MetricsService metricsService;

    public boolean executeBatch(String name, int day, List<Put> actions) {
        String tableName = hBaseClientFactory.getTableName(name, day);
        if (actions == null || actions.isEmpty()) {
            return true;
        }
        int tryCount = 0;
        Table table = null;
        long start = System.currentTimeMillis();
        try {
            metricsService.hBaseBatchPutCount(tableName, actions.size());
            while (!actions.isEmpty()) {
                tryCount++;
                if (tryCount > 1) {
                    Thread.sleep(1000);
                }
                Object[] results = new Object[actions.size()];
                try {
                    if (table == null) {
                        table = hBaseClientFactory.getOrAddTable(tableName);
                    }
                    table.batch(actions, results);
                } catch (IOException e) {
                    metricsService.hbaseFail();
                }
                for (int i = results.length - 1; i >= 0; i--) {
                    if (results[i] instanceof Result) {
                        actions.remove(i);
                    }
                }
            }
            return true;
        } catch (InterruptedException e) {
            hBaseClientFactory.closeCurrentThreadHTable(tableName);
        } finally {
            metricsService.hBaseBatchDuration(tableName, System.currentTimeMillis() - start);
        }
        return false;
    }
}
