package io.etrace.collector.metrics;

public interface MetricName {
    /**
     * network
     */
    String UNFRAMED_MESSAGE = "unframed.message";
    String THROUGHPUT = "throughput";
    String AGENT_THROUGHPUT = "agent.throughput";
    String AGENT_LATENCY = "agent.latency";
    String AGENT_FORBIDDEN = "agent.forbidden";

    /**
     * worker
     */
    String MESSAGE_ERROR = "work.error";
    String METRIC_FORBIDDEN = "metric.forbidden";

    /**
     * sink
     */
    String KAFKA_PRODUCER_TIME = "kafka.producer.duration";
    String KAFKA_SEND_ERROR = "kafka.send.error";

    /**
     * http
     */
    String COLLECTOR_ADDRESS_LIST = "collector.list.address";

}
