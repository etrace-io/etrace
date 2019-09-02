package io.etrace.common.modal;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class SearchResult {
    private List<SpanItem> items = new ArrayList<>();
    private long totalSize = 0;
    private int size = 0;
    private long lastTimestamp;
    private boolean isTimeOut = false;
    private boolean hasMoreResult = true;
    private String lastScanRowKey;

    public String getLastScanRowKey() {
        return lastScanRowKey;
    }

    public void setLastScanRowKey(String lastScanRowKey) {
        this.lastScanRowKey = lastScanRowKey;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public boolean isTimeOut() {
        return isTimeOut;
    }

    public void setTimeOut(boolean isTimeOut) {
        this.isTimeOut = isTimeOut;
    }

    public boolean isHasMoreResult() {
        return hasMoreResult;
    }

    public void setHasMoreResult(boolean hasMoreResult) {
        this.hasMoreResult = hasMoreResult;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public List<SpanItem> getItems() {
        return items;
    }

    public void setItems(List<SpanItem> items) {
        this.items = items;
    }

    public void addItem(SpanItem item) {
        items.add(item);
    }
}
