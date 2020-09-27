package io.etrace.common.sharding;

public interface ShardingStrategy {

    void init(int size);

    int chooseTasks(Object key);

    String name();

}
