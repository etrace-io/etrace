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

package io.etrace.collector.controller;

import io.etrace.collector.cluster.ClusterService;
import io.etrace.collector.cluster.discovery.ServiceDiscovery;
import io.etrace.collector.cluster.discovery.ServiceInstance;
import io.etrace.collector.metrics.MetricsService;
import io.etrace.collector.service.ClientConfigurationService;
import io.etrace.collector.util.Convertor;
import io.etrace.common.message.agentconfig.Collector;
import io.etrace.common.message.agentconfig.CollectorItem;
import io.etrace.common.pipeline.PipelineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collector")
public class CollectorAddressController {
    @Autowired
    public ServiceDiscovery serviceDiscovery;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private ClientConfigurationService clientConfigService;

    /**
     * @param cluster 集群
     * @return {@link Collection}
     */
    @GetMapping("/tcp/cluster")
    public Collection<Collector> getTcpCollectors(@RequestParam("cluster") String cluster) {
        // all port
        Set<ServiceInstance> instances = serviceDiscovery.queryForInstances(cluster);

        List<ServiceInstance> tcpInstances = instances.stream().filter(
            in -> in.getServerType().equalsIgnoreCase(PipelineConfiguration.Channel.Type.TCP.toString().toLowerCase()))
            .collect(Collectors.toList());

        //return tcp collector
        Collections.shuffle(tcpInstances);
        return Convertor.instance2Collector(tcpInstances);
    }

    /**
     * @param appId 应用程序Id
     * @param host  主机
     * @return {@link Collection}
     */
    @GetMapping
    public Collection<Collector> getThriftCollectors(@RequestParam(name = "appId", required = false) String appId,
                                                     @RequestParam(name = "host", required = false) String host) {
        metricsService.httpRequestCounter(appId, "/collector");
        //thrift port
        List<ServiceInstance> collectors = clusterService.getCollectors(appId,
            PipelineConfiguration.Channel.Type.THRIFT.toString().toLowerCase());

        Collections.shuffle(collectors);
        return Convertor.instance2Collector(collectors);
    }

    /**
     * @return {@link Map}
     */
    @GetMapping("/all")
    public Map<String, Set<ServiceInstance>> getAllCollectors() {
        return serviceDiscovery.getAllInstances();
    }

    /**
     * @param appId 应用程序Id
     * @param host  主机
     * @return {@link CollectorItem}
     */
    @GetMapping("/item")
    public CollectorItem getCollectorItem(@RequestParam("appId") String appId, @RequestParam("host") String host) {
        metricsService.httpRequestCounter(appId, "/collector/item");

        List<ServiceInstance> collectors = clusterService.getCollectors(appId, null);

        List<ServiceInstance> thriftCollectors = collectors.stream()
            .filter(in -> in.getServerType().equalsIgnoreCase(PipelineConfiguration.Channel.Type.THRIFT.toString()))
            .collect(Collectors.toList());

        List<ServiceInstance> tcpCollectors = collectors.stream()
            .filter(in -> in.getServerType().equalsIgnoreCase(PipelineConfiguration.Channel.Type.TCP.toString()))
            .collect(Collectors.toList());

        CollectorItem item = new CollectorItem();
        item.setUseTcp(clientConfigService.getTcpConfig(appId));
        item.setTcpCollector(Convertor.instance2Collector(tcpCollectors));
        item.setThriftCollector(Convertor.instance2Collector(thriftCollectors));
        return item;
    }

}
