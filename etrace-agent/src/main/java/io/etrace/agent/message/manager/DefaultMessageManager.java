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

package io.etrace.agent.message.manager;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.RequestIdAndRpcIdFactory;
import io.etrace.agent.message.callstack.CallstackQueue;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.trace.*;
import io.etrace.common.message.trace.impl.EventImpl;
import io.etrace.common.message.trace.impl.TransactionImpl;

import java.util.*;

import static io.etrace.common.constant.Constants.ROOT_RPC_ID;

public class DefaultMessageManager implements TraceManager {

    private CallstackQueue callstackQueue;
    private RequestIdAndRpcIdFactory requestIdAndRpcIdFactory;
    private ConfigManger configManger;
    private ThreadLocal<Context> contextThreadLocal;
    private ThreadLocal<Boolean> isImport = new ThreadLocal<>();

    @Inject
    public DefaultMessageManager(CallstackQueue callstackQueue, RequestIdAndRpcIdFactory requestIdAndRpcIdFactory,
                                 ConfigManger configManger) {
        this(callstackQueue, requestIdAndRpcIdFactory, configManger, true);
    }

    DefaultMessageManager(CallstackQueue callstackQueue, RequestIdAndRpcIdFactory requestIdAndRpcIdFactory,
                          ConfigManger configManger,
                          boolean createNewContext) {
        this.configManger = configManger;
        this.callstackQueue = callstackQueue;
        this.requestIdAndRpcIdFactory = requestIdAndRpcIdFactory;
        if (createNewContext) {
            this.contextThreadLocal = new ThreadLocal<>();
        }
    }

    @Override
    public void addNonTransaction(Message message) {
        Context ctx = getContext();
        if (ctx != null) {
            ctx.add(message);
        }
    }

    @Override
    public void endTransaction(Transaction transaction) {
        Context ctx = getContext();
        if (ctx != null) {
            if (ctx.end(transaction)) {
                removeContext();
            }
        }
    }

    @Override
    public ConfigManger getConfigManager() {
        return configManger;
    }

    @Override
    public void startTransaction(Transaction transaction) {
        Context ctx = getContext();
        if (ctx != null) {
            ctx.start(transaction);
        }
    }

    @Override
    public void shutdown() {
        configManger.shutdown();
        callstackQueue.shutdown();
    }

    @Override
    public boolean hasTransaction() {
        Context ctx = contextThreadLocal.get();
        if (ctx == null) {
            return false;
        }
        return ctx.getRoot() != null;
    }

    @Override
    public void setup() {
        contextThreadLocal.set(new Context());
    }

    @Override
    public void setup(String requestId, String rpcId) {
        contextThreadLocal.set(new Context(requestId, rpcId));
    }

    @Override
    public TraceContext exportContext() {
        Context ctx = getContext();
        if (ctx != null) {
            return new DefaultTraceContext(ctx);
        }
        return null;
    }

    @Override
    public void importContext(TraceContext ctx) {
        if (ctx != null && ctx.getCtx() instanceof Context) {
            contextThreadLocal.set((Context)ctx.getCtx());
            isImport.set(true);
        }
    }

    @Override
    public String getClientAppId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getClientAppId();
        }
        return Constants.UNKNOWN_APP_ID;
    }

    @Override
    public boolean hasContext() {
        return contextThreadLocal != null && contextThreadLocal.get() != null;
    }

    @Override
    public boolean isImportContext() {
        return isImport.get() != null && isImport.get();
    }

    @Override
    public void reset() {
        Context ctx = getContext();
        if (ctx != null) {
            if (ctx.totalDuration == 0) {
                ctx.stack.clear();
                ctx.knownExceptions.clear();
                removeContext();
            } else {
                ctx.knownExceptions.clear();
            }
        }
    }

    @Override
    public void removeContext() {
        if (contextThreadLocal != null) {
            contextThreadLocal.remove();
        }
        isImport.remove();
    }

    @Override
    public String nextRemoteRpcId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.nextRemoteRpcId();
        }
        return ROOT_RPC_ID;
    }

    @Override
    public String nextLocalRpcId() {
        String currentRpcId = getContext().rpcId;
        return RequestIdAndRpcIdFactory.buildNextLocalRpcId(currentRpcId, nextLocalThreadId());
    }

    @Override
    public String getCurrentRequestId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getCurrentRequestId();
        }
        return generateRequestId(null);
    }

    public String nextLocalThreadId() {
        Context ctx = getContext();
        if (ctx != null) {
            int nexLocalThreadId = ++ctx.nexLocalThreadId;
            return String.valueOf(nexLocalThreadId);
        }
        return String.valueOf(Thread.currentThread().getId());
    }

    @Override
    public String getRpcId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getRpcId();
        }
        return ROOT_RPC_ID;
    }

    @Override
    @Deprecated
    public String getCurrentRpcIdAndCurrentCall() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getCurrentRpcIdAndCurrentCall();
        }
        return ROOT_RPC_ID;
    }

    public boolean shouldLog(Throwable throwable) {
        Context ctx = contextThreadLocal == null ? null : contextThreadLocal.get();
        return ctx == null || ctx.shouldLog(throwable);
    }

    private void flush(String requestId, String id, Message message) {
        callstackQueue.produce(requestId, id, message);
        //reset current thread local data
        reset();
    }

    protected Context getContext() {
        Context ctx = contextThreadLocal.get();
        if (ctx == null) {
            ctx = new Context();
            contextThreadLocal.set(ctx);
        }
        return ctx;
    }

    /**
     * Request id must include root app id and create timestamp Format: RootAppId^^id|timestamp
     *
     * @param requestId request id
     * @return new request id
     */
    private String generateRequestId(String requestId) {
        return RequestIdAndRpcIdFactory.buildRequestId(
            Strings.isNullOrEmpty(requestId) ? requestIdAndRpcIdFactory.getNextId() : requestId,
            AgentConfiguration.getAppId());
    }

    public class Context {
        protected Stack<Transaction> stack;
        protected Set<Throwable> knownExceptions;
        /**
         * for truncate message;
         */
        protected long totalDuration;
        protected int nexLocalThreadId = 0;
        private Message root;
        private int currentCall = 0;
        /**
         * purely rpcId part, no "appId|" part
         */
        private String rpcId;
        private String requestId;
        private int next;
        private long totalChildren = 1;
        private String clientAppId;

        /**
         * 当调用者为Trace.newTransaction,complete时,考虑以下几种情况: 1. 最简单的情况,即同步调用<code>Trace.newTransaction()</code>
         * 可以方便地和鹰眼同步的api协同 2. MultiMessageProducer时 默认关闭打鹰眼指标的功能 高级用法(不推荐)可以通过调用<code>setForceCallEagleAPI(true)
         * </code>可以开启异步模式下调用鹰眼
         * 使用方必须保证线程上下文里没有鹰眼的指标调用,否则链路层级会错乱
         */
        public Context() {
            this(null, ROOT_RPC_ID);
        }

        public Context(String requestId, String rpcId) {
            stack = new Stack<>();
            this.knownExceptions = new HashSet<>();
            setup(requestId, rpcId);

        }

        public void setup(String requestId, String rpcId) {
            this.requestId = generateRequestId(requestId);
            if (Strings.isNullOrEmpty(rpcId)) {
                rpcId = ROOT_RPC_ID;
            }
            this.clientAppId = RequestIdAndRpcIdFactory.parseClientAppIdFromOriginalRpcId(rpcId);
            this.rpcId = RequestIdAndRpcIdFactory.parseRpcIdFromOriginalRpcId(rpcId);
        }

        public String nextRemoteRpcId() {
            currentCall++;
            return RequestIdAndRpcIdFactory.buildNextRemoteRpcId(rpcId, currentCall);
        }

        public String getClientAppId() {
            if (Strings.isNullOrEmpty(clientAppId)) {
                return Constants.UNKNOWN_APP_ID;
            }
            return clientAppId;
        }

        public String getCurrentRequestId() {
            return requestId;
        }

        public String getRpcId() {
            return rpcId;
        }

        public String getCurrentRpcIdAndCurrentCall() {
            if (currentCall == 0) {
                return rpcId;
            }
            return RequestIdAndRpcIdFactory.buildNextRemoteRpcId(rpcId, currentCall);
        }

        public void add(Message message) {
            if (stack.isEmpty()) {
                //only has event in this call stack
                flush(requestId, rpcId, message);
            } else {
                //only transaction span add into call stack
                addChildren(stack.peek(), message);
            }
        }

        public boolean shouldLog(Throwable throwable) {
            if (knownExceptions == null) {
                knownExceptions = new HashSet<>();
            }
            if (knownExceptions.contains(throwable)) {
                return false;
            } else {
                knownExceptions.add(throwable);
                return true;
            }
        }

        public void start(Transaction transaction) {
            if (stack.isEmpty()) {
                root = transaction;
            } else {
                addChildren(stack.peek(), transaction);
            }

            stack.push(transaction);
        }

        public boolean end(Transaction transaction) {
            if (stack != null && !stack.isEmpty()) {
                if (transaction.isBadTransaction() && transaction != root) {
                    return false;
                }
                Transaction current = stack.pop();
                if (current != transaction) {
                    while (transaction != current && !stack.isEmpty()) {
                        if (!current.isBadTransaction()) {
                            Event event = new EventImpl(Constants.AGENT_EVENT_TYPE, Constants.NAME_BAD_TRANSACTION);
                            event.setStatus("TransactionNotCompleted");
                            event.complete();
                            current.addChild(event);
                        }
                        current = stack.pop();
                    }
                }

                if (stack.isEmpty()) {
                    if (totalDuration > 0) {
                        totalDuration = 0;
                    }
                    // flush会reset context, 所以一些线程上下文相关的逻辑需要写在前面
                    flush(requestId, rpcId, root);
                    return true;
                }
            }
            return false;
        }

        protected Message getRoot() {
            return root;
        }

        private void addChildren(Transaction parent, Message child) {
            if (totalChildren >= configManger.getMessageCount()) {
                truncateAndFlush((Transaction)root);
                totalChildren = 1;
                parent = (Transaction)root;
            }
            parent.addChild(child);
            totalChildren++;
        }

        private void truncateAndFlush(Transaction parent) {
            String childId = RequestIdAndRpcIdFactory.buildTruncatedRpcId(rpcId, this.next);
            Transaction target = new TransactionImpl("Trace", Constants.NAME_TRUNCATE);
            target.setStatus(Message.SUCCESS);

            Event event = new EventImpl(Constants.AGENT_EVENT_TYPE, Constants.NAME_BAD_TRANSACTION);
            event.setStatus("TransactionLongerChildren");
            event.complete();
            target.addChild(event);

            List<Message> children = parent.getChildren();
            Iterator<Message> it = children.iterator();
            List<Message> truncateEvents = new ArrayList<>();
            while (it.hasNext()) {
                Message message = it.next();
                if (!(Constants.TYPE_ETRACE_LINK.equals(message.getType()) && Constants.NAME_TRUNCATE.equals(
                    message.getName()))) {
                    if (!message.isCompleted()) {
                        message.complete();
                    }
                    target.addChild(message);
                } else {
                    truncateEvents.add(message);
                }
            }

            if (truncateEvents.size() > 100) {
                for (Message message : truncateEvents) {
                    target.addChild(message);
                }
                truncateEvents.clear();
            }

            this.next++;
            totalDuration = 1;//totalDuration + parent.getDuration();

            flush(getCurrentRequestId(), childId, target);
            Event next = new EventImpl(Constants.TYPE_ETRACE_LINK, Constants.NAME_TRUNCATE);
            next.setData(childId);
            next.setStatus(Message.SUCCESS);
            parent.setChildren(truncateEvents);
            parent.addChild(next);
        }
    }
}
