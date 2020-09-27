package io.etrace.collector.cluster.discovery;

import io.etrace.common.util.JSONUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InstanceSerializer {

    public byte[] serialize(ServiceInstance serviceInstance) throws IOException {
        return JSONUtil.toBytes(serviceInstance);
    }

    public ServiceInstance deserialize(byte[] data) throws IOException {
        return JSONUtil.toObject(data, ServiceInstance.class);
    }

}
