package io.etrace.collector.worker.impl;

import io.etrace.collector.metrics.MetricsService;
import io.etrace.plugins.kafka0882.impl.impl.producer.model.AsyncCallback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class KafkaCallback extends AsyncCallback {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaCallback.class);

    private int leader;
    private MetricsService metricsService;

    public KafkaCallback(int leader, MetricsService metricsService) {
        this.leader = leader;
        this.metricsService = metricsService;
    }

    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception exception) {

        if (exception != null || null == recordMetadata) {
            metricsService.writeError(recordMetadata.topic(), leader);
            LOGGER.error("send kafka error", exception);
        } else {
            metricsService.writeLatency(recordMetadata.topic(), leader,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st));
        }
    }
}
