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

package io.etrace.common.datasource;

import io.etrace.common.exception.EsperConfigException;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.pipeline.Resource;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public interface MetricDatasourceService {

    /**
     * validate whether has connected to given datasource by datasourceUniqueId
     *
     * @param datasourceUniqueId
     * @return
     */
    boolean connected(Long datasourceUniqueId);

    /**
     * connect related datasource
     *
     * @param datasourceUniqueId
     * @param datasourceName
     * @param config
     */
    void doConnect(Long datasourceUniqueId, String datasourceName, List<OneDatasourceConfig> config);

    void closeConnection(Long datasourceUniqueId);

    void cleanConnectionCache(Long datasourceUniqueId, String oldDatabaseName);

    void checkConnectionAndRebuildConnection();

    void sq(Long datasourceUniqueId, String database, boolean rebuild, DeferredResult<MetricResultSet> resultSet,
            MetricQLBean qlBean, CountDownLatch latch) throws Exception;

    String generateSuggestQL(MetricBean bean);

    List<MetricQLBean> generateQLBean(MetricBean bean, Date date) throws Exception;

    // ===  这里应改成和  doConnect 一样的 初始化方式
    void initResource(List<Resource> resources);

    void start();

    void registerDatasourceCluster(String resourceId, Resource resource) throws EsperConfigException;

    // === write data to storage
    void writeData(String resourceId, String database, List<Metric> metrics) throws Exception;
}
