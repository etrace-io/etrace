package io.etrace.collector.component.processor;

import io.etrace.collector.sharding.BackendShardIng;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Component;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.Partition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TraceProcessor extends AbstractTraceWorker {
    @Autowired
    private BackendShardIng shardingService;

    public TraceProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public Partition getPartition(MessageHeader key, byte[] value) throws Exception {
        return shardingService.getPartition(key, value, getTopic());
    }
}
