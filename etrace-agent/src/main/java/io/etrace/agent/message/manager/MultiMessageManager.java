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

import com.google.inject.Injector;
import io.etrace.agent.Trace;
import io.etrace.agent.message.RequestIdAndRpcIdFactory;
import io.etrace.agent.message.callstack.CallstackQueue;
import io.etrace.agent.module.InjectorFactory;
import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.trace.TraceContext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiMessageManager extends DefaultMessageManager {
    private TraceContext traceContext;
    private Lock lock = new ReentrantLock();

    /**
     * 2.x的实现为, 当调用setup()或reset()时, 会调用Trace.continueTrace或Trace.clean() 但逻辑上其实MultiMessageManager用于多线程情况下,
     * 并不需要和当前线程有所交互 2.x的实现会"意外地"影响当前线程的context
     * <p>
     * 由于历史如此, 为保证兼容性,3.0.0开始和集团hsf融合后, 添加新的boolean参数 用于和当前threadlocal的context做隔离 当<code>isolated = true</code>时,
     * 调用就不会影响到当前线程的context了
     *
     * @since 3.0.0
     */
    private boolean isolated = false;

    private MultiMessageManager(CallstackQueue callstackQueue, RequestIdAndRpcIdFactory requestIdAndRpcIdFactory,
                                ConfigManger configManger) {
        super(callstackQueue, requestIdAndRpcIdFactory, configManger, false);
        this.traceContext = new DefaultTraceContext(new Context());
    }

    public static MultiMessageManager createManager() {
        Injector injector = InjectorFactory.getInjector();
        CallstackQueue producer = injector.getInstance(CallstackQueue.class);
        RequestIdAndRpcIdFactory requestIdAndRpcIdFactory = injector.getInstance(RequestIdAndRpcIdFactory.class);
        ConfigManger configManger = injector.getInstance(ConfigManger.class);
        return new MultiMessageManager(producer, requestIdAndRpcIdFactory, configManger);
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }

    @Override
    public void setup() {
    }

    @Override
    public void setup(String requestId, String rpcId) {
        if (traceContext != null && traceContext.getCtx() instanceof Context) {
            Context ctx = (Context)traceContext.getCtx();
            ctx.setup(requestId, rpcId);
        }
        if (!isolated) {
            // only clean threadLocal Trace context by CallstackProducer
            Trace.continueTrace(requestId, rpcId);
        }
    }

    @Override
    public TraceContext exportContext() {
        lock.lock();
        try {
            if (traceContext != null) {
                return traceContext;
            }
            return super.exportContext();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean shouldLog(Throwable throwable) {
        Context ctx = traceContext == null ? null : (Context)traceContext.getCtx();
        return ctx == null || ctx.shouldLog(throwable);
    }

    @Override
    public void reset() {
        lock.lock();
        try {
            if (traceContext != null && traceContext.getCtx() instanceof Context) {
                Context ctx = (Context)traceContext.getCtx();
                if (ctx != null) {
                    if (ctx.totalDuration == 0) {
                        ctx.stack.clear();
                        ctx.knownExceptions.clear();
                        traceContext = null;
                    } else {
                        ctx.knownExceptions.clear();
                    }
                }
            }
            if (!isolated) {
                if (Trace.isImportContext()) {
                    Trace.clean();
                }
                super.reset();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected Context getContext() {
        lock.lock();
        try {
            if (traceContext == null || !(traceContext.getCtx() instanceof Context)) {
                traceContext = new DefaultTraceContext(new Context());
            }
            return (Context)traceContext.getCtx();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void importContext(TraceContext ctx) {
        if (ctx != null && ctx.getCtx() instanceof Context) {
            Context curCtx = (Context)ctx.getCtx();
            traceContext = new DefaultTraceContext(curCtx);

            // TODO do we need use isImport variable?
            //            isImport.set(true);
        }
    }

    @Override
    public boolean hasTransaction() {
        if (traceContext == null) {
            return false;
        }
        Context context = (Context)traceContext.getCtx();
        return context.getRoot() != null;
    }
}
