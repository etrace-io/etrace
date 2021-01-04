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

package io.etrace.consumer.storage.hbase.impl;

import io.etrace.consumer.metrics.MetricsService;
import io.etrace.consumer.storage.hbase.IHBaseClient;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class DefaultHBaseClient implements IHBaseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHBaseClient.class);

    @Autowired
    private IHBaseClientFactory hBaseClientFactory;
    @Autowired
    private MetricsService metricsService;

    @Override
    public boolean executeBatch(String logicTableName, String physicalTableName, List<Put> actions) {
        if (actions == null || actions.isEmpty()) {
            return true;
        }
        int tryCount = 0;
        HTable table = null;
        long start = System.currentTimeMillis();
        try {
            metricsService.hBaseBatchPutCount(physicalTableName, actions.size());
            while (!actions.isEmpty()) {
                tryCount++;
                if (tryCount > 1) {
                    Thread.sleep(1000);
                }
                Object[] results = new Object[actions.size()];
                try {
                    if (table == null) {
                        table = hBaseClientFactory.getOrCreateTable(logicTableName, physicalTableName);
                    }
                    table.batch(actions, results);
                } catch (IOException e) {
                    LOGGER.error("==executeBatch==", e);
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
            hBaseClientFactory.closeCurrentThreadHTable(physicalTableName);
        } finally {
            metricsService.hBaseBatchDuration(physicalTableName, System.currentTimeMillis() - start);
        }
        return false;
    }
}
