package io.etrace.common.modal;

import java.util.List;

public class MessageResult {
    private List<HostItem> items;
    private long size;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<HostItem> getItems() {
        return items;
    }

    public void setItems(List<HostItem> items) {
        this.items = items;
    }
}
