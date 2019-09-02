package io.etrace.common;

public interface Constants {
    String ROOT_RPC_ID = "1";
    String AGENT_EVENT_TYPE = "Trace";
    String UNKNOWN_APP_ID = "unknown";

    String UNSET = "unset";
    String SUCCESS = "0";
    String FAILURE = "failure";
    String URL = "URL";
    String URL_FORWARD = "URL.Forward";
    String TRACE_STATE = "trace-state";
    String TRACE_PAGE_URI = "trace-page-uri";
    String SQL = "SQL";
    String SQL_DATABASE = "SQL.database";
    String SQL_METHOD = "SQL.method";
    String CALL = "SOACall";
    String URL_CALL = "UrlCall";
    String CALL_SERVICE_APP = "SOACall.serviceApp";
    String CALL_SERVICE_IP = "SOACall.serviceIP";
    String CALL_SERVICE_RESULT = "SOACall.resultCode";
    String SERVICE_CLIENT_APP = "SOAService.clientApp";
    String SERVICE_CLIENT_IP = "SOAService.clientIP";
    String SERVICE_RESULT = "SOAService.resultCode";
    String SERVICE = "SOAService";
    String CACHE = "Cache";
    String CACHE_START = "Cache.";
    String CIRCUIT_BREAKER = "CircuitBreaker";
    String DOWNGRADE = "DownGrade";
    String HEART_BEAT = "Heartbeat";
    String RMQ_PRODUCE = "RMQ_PRODUCE";
    String RMQ_CONSUME = "RMQ_CONSUME";
    String EXCHANGE = "exchange";
    String SERVER = "server";
    String QUEUE = "queue";
    String RPC_ID = "rpcid";

    String TYPE_ETRACE_LINK = "ETraceLink";
    String NAME_REMOTE_CALL = "RemoteCall";
    String NAME_ASYNC_CALL = "AsyncCall";
    String NAME_TRUNCATE = "Truncate";
    String NAME_BAD_TRANSACTION = "BadTransaction";
    String REDIS_TYPE = "Redis";
    String REDIS_NAME = "Stats";
    String REDIS_TYPE_KEY = "redisType";
    String REDIS_TYPE_MIXED = "Mixed";
    String REDIS_TYPE_DEFAULT = "Corvus";
    String METRIC_TOP = "metric-";
    int ERROR_COUNT = 100;
    int MAX_MESSAGE_SIZE = 60;

    String ALL_MACHINE = "ALL";
    String ALL_IP = "0.0.0.0";

    String HEADER_REQUEST_ID = "X-Eleme-RequestID";
    String HEADER_RPC_ID = "X-Eleme-RpcID";
    String HEADER_APP_ID = "X-Eleme-AppID";

    String SUCCESS_STR = "success";
    String UNKNOWN = "unknown";
    String EXCEPTION_COUNT = "exception_count";
    String ERROR_RATE = "error_rate";

    String DAL_SQL = "dal_sql";
    String DAL_GROUP = "dal_group";
    String APP_INSTANCE = "app_instance";
    String APP_DATABASE = "app_database";
    String APP_EXCEPTION = "app_exception";
    String APPLICATION = "application";
}
