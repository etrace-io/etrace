/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.agent.message.callstack;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.manager.DefaultMessageManager;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.*;
import io.etrace.common.message.trace.impl.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CallstackProducer {

    protected final static Event DUMMY_EVENT = new DummyEvent();
    protected final static Transaction DUMMY_TRANSACTION = new DummyTransaction();
    protected final static Heartbeat DUMMY_HEARTBEAT = new DummyHeartbeat();
    private static AtomicLong sequence = new AtomicLong(0);

    protected TraceManager traceManager;

    @Inject
    public CallstackProducer(TraceManager traceManager) {
        this.traceManager = traceManager;
    }

    public void logError(Throwable throwable) {
        logError(null, throwable, null);
    }

    public void logError(String message, Throwable throwable) {
        logError(message, throwable, null);
    }

    public void logError(String message, Throwable throwable, Map<String, String> tags) {
        //need check enabled first
        if (!traceManager.getConfigManager().isEnabled()) {
            return;
        }
        if (!shouldLog(throwable)) {
            return;
        }

        Event event;
        try {
            if (throwable instanceof Error) {
                event = newEvent("Error", throwable.getClass().getName(), message, throwable);
            } else if (throwable instanceof RuntimeException) {
                event = newEvent("RuntimeException", throwable.getClass().getName(), message, throwable);
            } else {
                event = newEvent("Exception", throwable.getClass().getName(), message, throwable);
            }
            event.setStatus("ERROR");
            addTagsToMessage(tags, event);
            event.complete();
        } catch (Throwable ignore) {

        }
    }

    private Event newEvent(String type, String name, String message, Throwable t) {
        if (!traceManager.getConfigManager().isEnabled()) {
            return DUMMY_EVENT;
        }
        if (!traceManager.hasContext()) {
            traceManager.setup();
        }
        Event event = new EventImpl(type, name, message, t, traceManager);
        event.setId(sequence.incrementAndGet());
        return event;
    }

    public void logEvent(String type, String name, String status, String data, Map<String, String> tags) {
        if (!traceManager.getConfigManager().isEnabled()) {
            return;
        }
        Event event = newEvent(type, name, status);
        if (!Strings.isNullOrEmpty(status)) {
            event.setStatus(status);
        }
        event.setData(data);
        addTagsToMessage(tags, event);
        event.complete();
    }

    @Deprecated
    public Event newEvent(String type, String name) {
        return newEvent(type, name, Constants.UNSET);
    }

    private Event newEvent(String type, String name, String status) {
        if (!traceManager.getConfigManager().isEnabled()) {
            return DUMMY_EVENT;
        }
        if (!traceManager.hasContext()) {
            traceManager.setup();
        }
        Event event = new EventImpl(type, name, traceManager);
        event.setId(sequence.incrementAndGet());
        event.setStatus(status);
        return event;
    }

    public Transaction newTransaction(String type, String name) {
        if (!traceManager.getConfigManager().isEnabled()) {
            return DUMMY_TRANSACTION;
        }
        if (!traceManager.hasContext()) {
            traceManager.setup();
        }

        TransactionImpl transaction = new TransactionImpl(type, name, traceManager);

        if (AgentConfiguration.getGlobalTags() != null) {
            for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                transaction.addTag(entry.getKey(), entry.getValue());
            }
        }
        traceManager.startTransaction(transaction);
        transaction.setId(sequence.incrementAndGet());
        return transaction;
    }

    public void logHeartbeat(String type, String name, String status, String data, Map<String, String> tags) {
        if (!traceManager.getConfigManager().isEnabled()) {
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
        if (!traceManager.getConfigManager().isEnabled()) {
            return DUMMY_HEARTBEAT;
        }
        if (!traceManager.hasContext()) {
            traceManager.setup();
        }
        Heartbeat heartbeat = new HeartbeatImpl(type, name, traceManager);
        heartbeat.setId(sequence.incrementAndGet());
        addTagsToMessage(Collections.emptyMap(), heartbeat);
        return heartbeat;
    }

    public void shutdown() {
        traceManager.shutdown();
    }

    public String getCurrentRequestId() {
        return traceManager.getCurrentRequestId();
    }

    public String getCurrentRpcIdAndCurrentCall() {
        return traceManager.getCurrentRpcIdAndCurrentCall();
    }

    public String getRpcId() {
        return traceManager.getRpcId();
    }

    public void removeContext() {
        traceManager.removeContext();
    }

    public String nextLocalRpcId() {
        return traceManager.nextLocalRpcId();
    }

    public String nextRemoteRpcId() {
        return traceManager.nextRemoteRpcId();
    }

    public boolean hasContext() {
        return traceManager.hasContext();
    }

    public boolean hasTransaction() {
        return traceManager.hasTransaction();
    }

    public void clean() {
        traceManager.reset();
    }

    public void continueTrace(String requestId, String rpcId) {
        traceManager.setup(requestId, rpcId);
    }

    private boolean shouldLog(Throwable e) {
        return !(traceManager instanceof DefaultMessageManager) || ((DefaultMessageManager)traceManager).shouldLog(
            e);
    }

    public TraceContext exportContext() {
        return traceManager.exportContext();
    }

    public void importContext(TraceContext context) {
        traceManager.importContext(context);
    }

    public boolean isImportContext() {
        return traceManager.isImportContext();
    }

    public String getClientAppId() {
        return traceManager.getClientAppId();
    }

    private void addTagsToMessage(Map<String, String> tags, Message message) {
        if (tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                message.addTag(entry.getKey(), entry.getValue());
            }
        }
        // global tag has higher priority, may override given tags.
        if (AgentConfiguration.getGlobalTags() != null) {
            for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                message.addTag(entry.getKey(), entry.getValue());
            }
        }
    }

}
