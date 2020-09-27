package io.etrace.collector.component.exporter;

import io.etrace.collector.metrics.MetricsService;
import io.etrace.collector.service.ChannelManager;
import io.etrace.collector.worker.impl.KafkaCallback;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaCluster;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaEmitter;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaManager;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.RecordInfo;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KafkaExporter extends DefaultSyncTask {
    @Autowired
    ChannelManager channelManager;
    @Autowired
    MetricsService metricsService;
    private String resourceId;
    private Resource resource;

    public KafkaExporter(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        resourceId = (String)params.get("resourceId");
    }

    @Override
    public void init(Object... param) {

        List<Resource> resources = (List<Resource>)param[1];
        resource = resources.stream()
            .filter(r -> r.getName().equals(resourceId)).findFirst().get();
    }

    @Override
    public void startup() {
        super.startup();
        KafkaCluster kafkaCluster = channelManager.initKafkaCluster(resource.getName(), resource.getProps());

        String[] topics = resource.getProps().get("topics").split(",");
        Arrays.stream(topics).forEach(topic -> KafkaManager.getInstance().register(topic, kafkaCluster));
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        int leader = (int)key;
        KafkaEmitter.send(new RecordInfo(leader, new KafkaCallback(leader, metricsService), (ProducerRecord)event));
    }

}
