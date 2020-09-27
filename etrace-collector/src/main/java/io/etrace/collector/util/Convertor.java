package io.etrace.collector.util;

import io.etrace.collector.cluster.discovery.ServiceInstance;
import io.etrace.common.message.agentconfig.Collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Convertor {

    public static List<Collector> instance2Collector(Collection<ServiceInstance> instances) {
        List<Collector> collectors = new ArrayList<>(instances.size());
        for (ServiceInstance instance : instances) {
            collectors.add(instance2Collector(instance));
        }
        return collectors;
    }

    private static Collector instance2Collector(ServiceInstance instance) {
        return new Collector(instance.getAddress(), instance.getPort());
    }
}
