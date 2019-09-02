package io.etrace.agent.message;

import io.etrace.agent.message.manager.MultiMessageManager;
import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.RedisResponse;
import io.etrace.common.modal.Transaction;

import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiMessageProducer extends MessageProducer {
    private Stack<Transaction> transactions;
    private Lock lock = new ReentrantLock();

    private MultiMessageProducer(MessageManager messageManager) {
        super(messageManager);
        transactions = new Stack<>();
    }

    public static MultiMessageProducer createProducer() {
        MultiMessageManager multiMessageManager = MultiMessageManager.createManager();
        return new MultiMessageProducer(multiMessageManager);
    }

    /**
     * 用于和hsf融合时, setup和reset不会影响threadlocal的context
     *
     * @see MultiMessageManager
     * @since 3.0.0
     */
    public static MultiMessageProducer createIsolatedProducer() {
        MultiMessageManager multiMessageManager = MultiMessageManager.createManager();
        multiMessageManager.setIsolated(true);
        return new MultiMessageProducer(multiMessageManager);
    }

    @Override
    protected boolean enableElog() {
        return false;
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

    public void redis(String url, String command, long duration, boolean succeed, String redisType) {
        super.redis(url, command, duration, succeed, null, redisType);
    }

    public void redis(String url, String command, long duration, boolean succeed, RedisResponse response,
                      String redisType) {
        super.redis(url, command, duration, succeed, new RedisResponse[] {response}, redisType);
    }
}
