package io.etrace.common.modal.impl;

import io.etrace.common.Constants;
import io.etrace.common.modal.Message;
import io.etrace.common.modal.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionEmpty implements Transaction {
    private static final String type = "empty";
    private static final String name = "empty";
    private static final String status = Constants.UNSET;

    @Override
    public void addChild(Message message) {

    }

    @Override
    public List<Message> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public void setChildren(List<Message> children) {

    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void setDuration(long duration) {

    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {

    }

    @Override
    public Map<String, String> getTags() {
        return new HashMap<>();
    }

    @Override
    public void setTags(Map<String, String> tags) {

    }

    @Override
    public void addTag(String key, String value) {

    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void complete() {

    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public void setStatus(Throwable e) {

    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public boolean isBadTransaction() {
        return true;
    }
}

