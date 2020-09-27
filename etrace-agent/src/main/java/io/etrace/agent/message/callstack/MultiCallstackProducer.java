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

import io.etrace.agent.message.manager.MultiMessageManager;
import io.etrace.common.message.trace.TraceManager;
import io.etrace.common.message.trace.Transaction;

import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiCallstackProducer extends CallstackProducer {
    private Stack<Transaction> transactions;
    private Lock lock = new ReentrantLock();

    private MultiCallstackProducer(TraceManager traceManager) {
        super(traceManager);
        transactions = new Stack<>();
    }

    public static MultiCallstackProducer createProducer() {
        MultiMessageManager multiMessageManager = MultiMessageManager.createManager();
        // todo: 默认 MultiMessageManager 也应是setIsolated(true);
        return new MultiCallstackProducer(multiMessageManager);
    }

    /**
     * 用于和hsf融合时, setup和reset不会影响threadlocal的context
     *
     * @return {@link MultiCallstackProducer}
     * @see MultiMessageManager
     * @since 3.0.0
     */
    public static MultiCallstackProducer createIsolatedProducer() {
        MultiMessageManager multiMessageManager = MultiMessageManager.createManager();
        multiMessageManager.setIsolated(true);
        return new MultiCallstackProducer(multiMessageManager);
    }

    public void startTransaction(String type, String name) {
        startTransactionAndGet(type, name);
    }

    public Transaction startTransactionAndGet(String type, String name) {
        lock.lock();
        try {
            Transaction t = newTransaction(type, name);
            transactions.push(t);
            return t;
        } finally {
            lock.unlock();
        }
    }

    public void addTag(String key, String value) {
        lock.lock();
        try {
            if (!transactions.isEmpty()) {
                Transaction t = transactions.peek();
                if (t != null) {
                    t.addTag(key, value);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void setTransactionStatus(Throwable throwable) {
        lock.lock();
        try {
            setTransactionStatus(throwable.getClass().getName());
        } finally {
            lock.unlock();
        }
    }

    public void setTransactionStatus(String status) {
        lock.lock();
        try {
            if (!transactions.isEmpty()) {
                Transaction t = transactions.peek();
                if (t != null) {
                    t.setStatus(status);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void completeTransaction() {
        lock.lock();
        try {
            if (!transactions.isEmpty()) {
                Transaction t = transactions.pop();
                if (t != null) {
                    t.complete();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void completeAllTransaction() {
        while (!transactions.isEmpty()) {
            completeTransaction();
        }
    }

    @Override
    public void clean() {
        lock.lock();
        try {
            this.transactions.clear();
            super.clean();
        } finally {
            lock.unlock();
        }
    }
}
