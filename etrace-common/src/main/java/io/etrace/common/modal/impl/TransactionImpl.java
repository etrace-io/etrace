package io.etrace.common.modal.impl;

import io.etrace.common.Constants;
import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.AbstractMessage;
import io.etrace.common.modal.Event;
import io.etrace.common.modal.Message;
import io.etrace.common.modal.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionImpl extends AbstractMessage implements Transaction {
    private long duration = 0;
    private boolean isBadTransaction = false;
    private List<Message> children;

    public TransactionImpl() {
    }

    public TransactionImpl(String type, String name) {
        this(type, name, null);
    }

    public TransactionImpl(String type, String name, MessageManager manager) {
        super(type, name, manager);
    }

    @Override
    public void addChild(Message message) {
        if (message == null) {
            return;
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        if (message instanceof Event && Constants.AGENT_EVENT_TYPE.equals(message.getType())
            && Constants.NAME_BAD_TRANSACTION.equals(message.getName())) {
            isBadTransaction = true;
        }
        children.add(message);
    }

    @Override
    public List<Message> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Message> children) {
        this.children = children;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean isBadTransaction() {
        return isBadTransaction;
    }

    @Override
    public void complete() {
        try {
            if (!isCompleted()) {
                duration = System.currentTimeMillis() - timestamp;
                setCompleted(true);
                if (manager != null) {
                    manager.end(this);
                }
            }
        } catch (Exception ignore) {
        }
    }
}
