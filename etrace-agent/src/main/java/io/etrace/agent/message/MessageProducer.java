package io.etrace.agent.message;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.manager.DefaultMessageManager;
import io.etrace.common.Constants;
import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.*;
import io.etrace.common.modal.impl.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MessageProducer {
    protected final static Event EMPTY_EVENT = new EventEmpty();
    protected final static Transaction EMPTY_TRANSACTION = new TransactionEmpty();
    protected final static Heartbeat EMPTY_HEARTBEAT = new HeartbeatEmpty();
    private static AtomicLong sequence = new AtomicLong(0);
    private static Class businessException;

    static {
        try {
            businessException = Class.forName("me.ele.contract.exception.ServiceException");
        } catch (ClassNotFoundException ignore) {
        }
    }

    protected MessageManager messageManager;

    @Inject
    public MessageProducer(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    /**
     * 该producer是否支持鹰眼的ELOG
     * <p>
     * 在MessageProducer是支持的, 但是在MultiMessageProducer是不支持的
     *
     * @since 3.0.0
     */
    protected boolean enableElog() {
        return true;
    }

    public void logError(Throwable throwable) {
        logError(null, throwable, null);
    }

    public void logError(String message, Throwable throwable) {
        logError(message, throwable, null);
    }

    public void logError(String message, Throwable throwable, Map<String, String> tags) {
        //need check enabled first
        if (!messageManager.getConfigManager().isEnabled()) {
            return;
        }
        if (!shouldLog(throwable)) {
            return;
        }

        Event event;
        try {
            if (businessException != null && businessException.isInstance(throwable)) {
                event = newEvent("BusinessException", throwable.getClass().getName(), message, throwable);
            } else if (throwable instanceof Error) {
                event = newEvent("Error", throwable.getClass().getName(), message, throwable);
            } else if (throwable instanceof RuntimeException) {
                event = newEvent("RuntimeException", throwable.getClass().getName(), message, throwable);
            } else {
                event = newEvent("Exception", throwable.getClass().getName(), message, throwable);
            }
            if (event != null) {
                event.setStatus("ERROR");
                if (tags != null) {
                    for (Map.Entry<String, String> entry : tags.entrySet()) {
                        event.addTag(entry.getKey(), entry.getValue());
                    }
                }
                if (AgentConfiguration.getGlobalTags() != null) {
                    for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                        event.addTag(entry.getKey(), entry.getValue());
                    }
                }
                event.complete();
            }
        } catch (Throwable ignore) {

        }
    }

    private Event newEvent(String type, String name, String message, Throwable t) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return EMPTY_EVENT;
        }
        if (!messageManager.hasContext()) {
            messageManager.setup();
        }
        Event event = new EventImpl(type, name, message, t, messageManager);
        event.setId(sequence.incrementAndGet());
        return event;
    }

    public void logEvent(String type, String name, String status, String data, Map<String, String> tags) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return;
        }
        Event event = newEvent(type, name, status);
        if (!Strings.isNullOrEmpty(status)) {
            event.setStatus(status);
        }
        event.setData(data);
        if (tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                event.addTag(entry.getKey(), entry.getValue());
            }
        }
        if (AgentConfiguration.getGlobalTags() != null) {
            for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                event.addTag(entry.getKey(), entry.getValue());
            }
        }
        event.complete();
    }

    public Event newEvent(String type, String name) {
        return newEvent(type, name, Constants.UNSET);
    }

    public Event newEvent(String type, String name, String status) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return EMPTY_EVENT;
        }
        if (!messageManager.hasContext()) {
            messageManager.setup();
        }
        Event event = new EventImpl(type, name, messageManager);
        event.setId(sequence.incrementAndGet());
        return event;
    }

    public Transaction newTransaction(String type, String name) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return EMPTY_TRANSACTION;
        }
        if (!messageManager.hasContext()) {
            messageManager.setup();
        }

        TransactionImpl transaction = new TransactionImpl(type, name, messageManager);

        if (AgentConfiguration.getGlobalTags() != null) {
            for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                transaction.addTag(entry.getKey(), entry.getValue());
            }
        }
        messageManager.start(transaction);
        transaction.setId(sequence.incrementAndGet());
        return transaction;
    }

    public void logHeartbeat(String type, String name, String status, String data, Map<String, String> tags) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return;
        }
        Heartbeat heartbeat = newHeartbeat(type, name);
        heartbeat.setStatus(status);
        heartbeat.setData(data);
        if (tags != null) {
            heartbeat.addTags(tags);
        }
        heartbeat.complete();
    }

    public Heartbeat newHeartbeat(String type, String name) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return EMPTY_HEARTBEAT;
        }
        if (!messageManager.hasContext()) {
            messageManager.setup();
        }
        Heartbeat heartbeat = new HeartbeatImpl(type, name, messageManager);
        if (AgentConfiguration.getGlobalTags() != null) {
            // 隐患：heartbeat 中新建HEARTBEAT的调用各不一样，用 addTags实际上会覆盖原有的maps
            heartbeat.addTags(AgentConfiguration.getGlobalTags());
        }
        heartbeat.setId(sequence.incrementAndGet());
        return heartbeat;
    }

    public void shutdown() {
        messageManager.shutdown();
    }

    public String getCurrentRequestId() {
        return messageManager.getCurrentRequestId();
    }

    public String getCurrentRpcId() {
        return messageManager.getCurrentRpcId();
    }

    public String getRpcId() {
        return messageManager.getRpcId();
    }

    public void removeContext() {
        messageManager.removeContext();
    }

    public String nextLocalRpcId() {
        return messageManager.nextLocalRpcId();
    }

    public String nextRemoteRpcId() {
        return messageManager.nextRemoteRpcId();
    }

    public boolean hasContext() {
        return messageManager.hasContext();
    }

    public boolean hasTransaction() {
        return messageManager.hasTransaction();
    }

    public void clean() {
        messageManager.reset();
    }

    public void continueTrace(String requestId, String rpcId) {
        messageManager.setup(requestId, rpcId);
    }

    private boolean shouldLog(Throwable e) {
        return !(messageManager instanceof DefaultMessageManager) || ((DefaultMessageManager)messageManager).shouldLog(
            e);
    }

    public TraceContext exportContext() {
        return messageManager.exportContext();
    }

    public void importContext(TraceContext context) {
        messageManager.importContext(context);
    }

    public boolean isImportContext() {
        return messageManager.isImportContext();
    }

    public String getClientAppId() {
        return messageManager.getClientAppId();
    }

    public void redis(String url, String command, long duration, boolean succeed, RedisResponse[] responses,
                      String redisType) {
        if (!messageManager.getConfigManager().isEnabled()) {
            return;
        }
        messageManager.addRedis(url, command, duration, succeed, redisType, responses);
    }
}
