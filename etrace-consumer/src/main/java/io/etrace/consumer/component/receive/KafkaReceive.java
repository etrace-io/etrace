package io.etrace.consumer.component.receive;

import io.etrace.common.channel.KafkaConsumerProp;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Receiver;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.plugins.kafka0882.impl.impl.consumer.AbstractConsumer;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KafkaReceive extends DefaultSyncTask implements Receiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReceive.class);

    private String resourceId;
    private AbstractConsumer consumer;
    private Resource resource;
    private KafkaConsumerProp kafkaConsumerProp;

    public KafkaReceive(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        this.resourceId = Optional.of(params.get("resourceId")).get().toString();

        kafkaConsumerProp = new KafkaConsumerProp();
        kafkaConsumerProp.setGroup((String)params.get("group"));

        kafkaConsumerProp.setTopics(params.get("topics").toString());
        kafkaConsumerProp.setNumStreams(Integer.valueOf(params.get("num").toString()));
    }

    @Override
    public void init(Object... param) {
        List<Resource> resources = (List<Resource>)param[1];
        resource = resources.stream().filter(r -> r.getName().equals(resourceId)).findFirst().get();
    }

    @Override
    public void startup() {
        super.startup();
        try {
            consumer = new AbstractConsumer(component.getPipeline()) {
                @Override
                public void onMessage(MessageAndMetadata<byte[], byte[]> data) {
                    dispatch(data);
                }
            };

            consumer.startup(resource.getProps(), kafkaConsumerProp);
        } catch (Exception e) {
            LOGGER.error("", e);
            System.exit(-1);
        }
    }

    public void dispatch(MessageAndMetadata<byte[], byte[]> data) {
        component.dispatchAll("", data);
    }

    @Override
    public void stop() {
        //
        this.consumer.destroy();
        super.stop();
    }
}
