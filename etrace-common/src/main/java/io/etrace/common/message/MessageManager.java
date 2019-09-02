package io.etrace.common.message;

import io.etrace.common.modal.Message;
import io.etrace.common.modal.RedisResponse;
import io.etrace.common.modal.TraceContext;
import io.etrace.common.modal.Transaction;

/**
 * This interface is internal usage, application developer should never call this method.
 */
public interface MessageManager {

    void addRedis(String url, String command, long duration, boolean succeed, String redisType,
                  RedisResponse[] responses);

    void add(Message message);

    void start(Transaction transaction);

    void end(Transaction transaction);

    void setup();

    void setup(String requestId, String rpcId);

    void importContext(TraceContext ctx);

    TraceContext exportContext();

    String getClientAppId();

    boolean hasContext();

    void removeContext();

    String nextRemoteRpcId();

    String nextLocalRpcId();

    String getCurrentRequestId();

    String getCurrentRpcId();

    boolean isImportContext();

    /**
     * Destroy current thread local data
     */
    void reset();

    ConfigManger getConfigManager();

    void shutdown();

    boolean hasTransaction();

    String getRpcId();
}
