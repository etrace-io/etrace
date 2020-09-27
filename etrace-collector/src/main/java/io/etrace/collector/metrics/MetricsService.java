package io.etrace.collector.metrics;

import com.google.common.base.Strings;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

import static io.etrace.collector.metrics.MetricName.*;

@Service
public class MetricsService {

    public CompositeMeterRegistry dynamicRegistry = new CompositeMeterRegistry();

    @Autowired
    private StepMeterRegistry metricsReporter;

    @PostConstruct
    public void startup() {
        Metrics.addRegistry(dynamicRegistry);
        dynamicRegistry.add(metricsReporter);
    }

    @PreDestroy
    public void stop() {
        metricsReporter.stop();
    }

    @Scheduled(fixedRate = 3600000)
    public void run() {
        for (Meter meter : dynamicRegistry.getMeters()) {
            dynamicRegistry.remove(meter);
        }
    }

    public void httpRequestCounter(String appId, String url) {
        dynamicRegistry.counter(COLLECTOR_ADDRESS_LIST,
            Tags.of("appId", !Strings.isNullOrEmpty(appId) ? "unknown" : appId, "url", url)).increment();
    }

    public void forbiddenThoughPut(String appId, int size) {
        dynamicRegistry.counter(AGENT_FORBIDDEN, Tags.of("agent", appId)).increment(size);
    }

    public void agentThoughPut(String appId, String messageType, int size) {
        dynamicRegistry.counter(AGENT_THROUGHPUT, Tags.of("agent", appId, "messageType", messageType)).increment(size);
    }

    public void agentLatency(String appId, long latency, String messageType) {
        dynamicRegistry.timer(AGENT_LATENCY, Tags.of("agent", appId, "messageType", messageType)).record(
            Duration.ofMillis(latency));
    }

    public void messageError(String name, String appId, String exception) {
        dynamicRegistry.counter(MESSAGE_ERROR, Tags.of("type", name, "agent", appId, "exception", exception))
            .increment();
    }

    public void writeLatency(String topic, int leader, long latency) {
        dynamicRegistry.timer(KAFKA_PRODUCER_TIME, Tags.of("topic", topic).and("leader", String.valueOf(leader)))
            .record(Duration.ofMillis(latency));
    }

    public void writeError(String topic, int leader) {
        dynamicRegistry.counter(KAFKA_SEND_ERROR, Tags.of("topic", topic).and("leader", String.valueOf(leader)))
            .increment();
    }

    public void forbiddenMetrics(String appId, int length) {
        dynamicRegistry.counter(METRIC_FORBIDDEN, Tags.of("agent", appId)).increment(length);
    }
}
