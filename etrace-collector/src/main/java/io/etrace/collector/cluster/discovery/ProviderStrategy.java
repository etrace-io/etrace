package io.etrace.collector.cluster.discovery;

import java.util.Set;

public interface ProviderStrategy {

    public Set<ServiceInstance> getInstance();
}
