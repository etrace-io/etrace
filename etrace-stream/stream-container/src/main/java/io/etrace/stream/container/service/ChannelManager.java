package io.etrace.stream.container.service;

import com.google.common.collect.Maps;
import io.etrace.common.datasource.MetricDatasourceService;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.pipeline.PipelineRepository;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.pipeline.impl.DefaultPipelineLoader;
import io.etrace.stream.container.config.ConfigProp;
import io.etrace.plugins.kafka0882.impl.impl.producer.ClusterBuilder;
import io.etrace.plugins.kafka0882.impl.impl.producer.KafkaCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class ChannelManager implements BeanFactoryAware {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);
    private final Map<String, KafkaCluster> kafkaClusters = Maps.newConcurrentMap();
    @Autowired
    private ConfigProp configProp;
    private PipelineRepository repository;
    private BeanFactory beanFactory;

    @Autowired
    private MetricDatasourceService metricDatasourceService;

    @PostConstruct
    public void startup() {
        try {
            repository = beanFactory.getBean(PipelineRepository.class, new DefaultPipelineLoader().load(),
                configProp.getResources());
            repository.initAndStartUp();

            metricDatasourceService.initResource(repository.getResources());
            metricDatasourceService.start();
        } catch (Exception e) {
            LOGGER.error("start up pipeline error: ", e);
            System.exit(-1);
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        repository.stop();
        LOGGER.info("Shutdown Stream Manager");
    }

    public synchronized KafkaCluster createCluster(String cluster, Resource resource) {
        if (kafkaClusters.containsKey(cluster)) {
            return kafkaClusters.get(cluster);
        }

        Properties props = new Properties();
        props.putAll(resource.getProps());

        LOGGER.info("Kafka producer config properties: [{}].", props);
        KafkaCluster kafkaCluster = new ClusterBuilder().clusterName(cluster)
            .producerConfig(props).build();

        kafkaClusters.put(cluster, kafkaCluster);
        return kafkaCluster;
    }

    public void writeData(String resourceId, String database, List<Metric> metrics) throws Exception {
        metricDatasourceService.writeData(resourceId, database, metrics);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
