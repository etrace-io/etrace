package io.etrace.common.modal;

import java.util.List;

public interface Transaction extends Message {
    void addChild(Message message);

    List<Message> getChildren();

    void setChildren(List<Message> children);

    long getDuration();

    void setDuration(long duration);

    boolean isBadTransaction();
}
