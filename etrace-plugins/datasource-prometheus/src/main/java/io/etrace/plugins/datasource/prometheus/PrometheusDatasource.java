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

package io.etrace.plugins.datasource.prometheus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.etrace.common.datasource.*;
import io.etrace.common.exception.EsperConfigException;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.pipeline.PipelineConfiguration;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.prometheus.client.*;
import io.prometheus.client.exporter.PushGateway;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * send metrics to Prometheus via PushGateway(https://github.com/prometheus/client_java#exporting-to-a-pushgateway)
 */
public class PrometheusDatasource implements MetricDatasourceService {

    Map<String, RegistryAndPushGateway> pushGatewayMap = Maps.newConcurrentMap();

    @Override
    public boolean connected(Long datasourceUniqueId) {
        return pushGatewayMap.containsKey(datasourceUniqueId);
    }

    @Override
    public void doConnect(Long datasourceUniqueId, String datasourceName, List<OneDatasourceConfig> config) {
        pushGatewayMap.putIfAbsent(datasourceName,
            new RegistryAndPushGateway(new CollectorRegistry(),
                config.stream().map(one -> {
                    PushGateway pushGateway = new PushGateway(one.getAddress());
                    return pushGateway;
                }).collect(Collectors.toList()), datasourceName)
        );
    }

    @Override
    public void closeConnection(Long datasourceUniqueId) {
        pushGatewayMap.remove(datasourceUniqueId);
    }

    @Override
    public void cleanConnectionCache(Long datasourceUniqueId, String oldDatabaseName) {
        // do nothing
    }

    @Override
    public void checkConnectionAndRebuildConnection() {
        // do nothing
    }

    @Override
    public void sq(Long datasourceUniqueId, String database, boolean rebuild, DeferredResult<MetricResultSet> resultSet,
                   MetricQLBean qlBean, CountDownLatch latch) throws Exception {

    }

    @Override
    public String generateSuggestQL(MetricBean bean) {
        return null;
    }

    @Override
    public List<MetricQLBean> generateQLBean(MetricBean bean, Date date) throws Exception {
        return null;
    }

    @Override
    public void initResourceAndStart(List<Resource> resources) {
        resources.forEach(resource -> {
                if (PipelineConfiguration.Channel.Type.PROMETHEUS.equals(resource.getType())) {
                    pushGatewayMap.putIfAbsent(resource.getName(),
                        new RegistryAndPushGateway(new CollectorRegistry(),
                            Lists.newArrayList(new PushGateway(resource.getProps().get("servers"))),
                            resource.getName()));
                }
            }
        );
    }

    @Override
    public void registerDatasourceCluster(String resourceId, Resource resource) throws EsperConfigException {

    }

    private Map<String, Collector> collectorMap = Maps.newConcurrentMap();

    @Override
    public void writeData(String datasourceUniqueId, String database, List<Metric> metrics) throws Exception {
        RegistryAndPushGateway registryAndPushGateway = pushGatewayMap.get(datasourceUniqueId);

        metrics.forEach(oneMetric -> {
            // replace all "." with "_"
            oneMetric.setMetricName(oneMetric.getMetricName().replace(".", "_"));

            switch (oneMetric.getMetricType()) {
                case Counter:
                case Ratio:
                    oneMetric.getFields().forEach((key, value) -> {
                        String metricName = oneMetric.getMetricName() + "_" + key;
                        String keyInCollectorMap = oneMetric.getMetricName() + "_" + key + "_COUNT";
                        Counter count = (Counter)collectorMap.computeIfAbsent(keyInCollectorMap,
                            name -> Counter.build()
                                .name(metricName)
                                .help("stream:" + NetworkInterfaceHelper.INSTANCE.getLocalHostAddress())
                                .labelNames(oneMetric.getTags().keySet().toArray(new String[0]))
                                .register(registryAndPushGateway.getRegistry()));
                        count.labels(oneMetric.getTags().values().toArray(new String[0]))
                            .inc(value.getValue());
                    });
                    break;
                case Gauge:
                    oneMetric.getFields().forEach((key, value) -> {
                        String metricName = oneMetric.getMetricName() + "_" + key;
                        String keyInCollectorMap = oneMetric.getMetricName() + "_" + key + "_GAUGE";
                        Gauge gauge = (Gauge)collectorMap.computeIfAbsent(keyInCollectorMap,
                            name -> Gauge.build()
                                .name(metricName)
                                .help("stream:" + NetworkInterfaceHelper.INSTANCE.getLocalHostAddress())
                                .labelNames(oneMetric.getTags().keySet().toArray(new String[0]))
                                .register(registryAndPushGateway.getRegistry())
                        );
                        gauge.labels(oneMetric.getTags().values().toArray(new String[0]))
                            .set(value.getValue());
                    });
                    break;
                case Timer:
                case Payload:
                case Histogram:
                    oneMetric.getFields().forEach((key, value) -> {
                        String metricName = oneMetric.getMetricName() + "_" + key;
                        String keyInCollectorMap = oneMetric.getMetricName() + "_" + key + "_HISTOGRAM";
                        Histogram histogram = (Histogram)collectorMap.computeIfAbsent(keyInCollectorMap,
                            name ->
                                Histogram.build()
                                    .name(metricName)
                                    .help("stream:" + NetworkInterfaceHelper.INSTANCE.getLocalHostAddress())
                                    .labelNames(oneMetric.getTags().keySet().toArray(new String[0]))
                                    .register(registryAndPushGateway.getRegistry())
                        );
                        histogram.labels(oneMetric.getTags().values().toArray(new String[0]))
                            .observe(value.getValue());
                    });
                    break;
                case Metric:
                    // not supported
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + oneMetric.getMetricType());
            }

        });

    }

    @Scheduled(initialDelay = 10 * 1000, fixedRate = 60 * 1000)
    private void pushToGateway() {
        pushGatewayMap.values().forEach(registryAndPushGateway -> {
            registryAndPushGateway.getPushGateway().forEach(pg -> {
                try {
                    pg.pushAdd(registryAndPushGateway.getRegistry(), registryAndPushGateway.database);
                } catch (IOException e) {
                    e.printStackTrace();
                    // todo: log ex
                }
            });

            // 清除数据
            registryAndPushGateway.getRegistry().clear();
        });
        // 清除数据
        collectorMap.clear();
    }

    @Data
    private static class RegistryAndPushGateway {
        private CollectorRegistry registry;
        private List<PushGateway> pushGateway;
        private String database;

        public RegistryAndPushGateway(CollectorRegistry registry, List<PushGateway> pushGateway, String database) {
            this.registry = registry;
            this.pushGateway = pushGateway;
            this.database = database;
        }
    }
}
