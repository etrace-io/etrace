package io.etrace.stream.biz.app;

import io.etrace.common.constant.Constants;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public interface EventConstant {

    String HEARTBEAT_TYPE_AGENT = "agent-stat";
    String HEARTBEAT_TYPE_HEARTBEAT = "Heartbeat";

    String TRANSACTION_TYPE_SQL = Constants.SQL;
    String TRANSACTION_TYPE_URL = Constants.URL;

    String JVM_MEMORY_POOL = "jvm.memory.pool";
    String JVM_MEMORY_HEAP = "jvm.memory";
    String JVM_GARBAGE_COUNT = "jvm.garbage.count";
    String JVM_GARBAGE_TIME = "jvm.garbage.time";
    String JVM_THREAD = "jvm.thread";
    String JVM_CPU = "jvm.cpu";
    String JVM_LOADED_CLASS = "jvm.loaded.classes";

    String EXCEPTION_METHOD = "method";
    String EXCEPTION_TAG_METHOD = "_method";
    String EXCEPTION_TAG_SOURCE_TYPE = "_sourceType";

    //todo: delete these two
    String DEFAULT_SOA_SERVICE_METHOD = "UNKNOWN";
    String DEFAULT_RMQ_CONSUMER_QUEUE = "UNKNOWN";

    String EVENT_TYPE_APP = "app";

    Set<String> VALID_HTTP_METHOD = newHashSet("option", "get", "put", "post", "delete", "head", "patch");

    String SERVICE_APP = "serviceApp";
    String CLIENT_APP = "clientApp";
}
