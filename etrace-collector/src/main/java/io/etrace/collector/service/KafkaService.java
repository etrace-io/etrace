package io.etrace.collector.service;

public interface KafkaService {

    public void addTopic(String name, String topic);

    public void startup();
}
