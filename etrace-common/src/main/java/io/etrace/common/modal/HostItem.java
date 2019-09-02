package io.etrace.common.modal;

import java.util.List;

public class HostItem {
    private String hostIp;
    private List<CallStackItem> callStackItems;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public List<CallStackItem> getCallStackItems() {
        return callStackItems;
    }

    public void setCallStackItems(List<CallStackItem> callStackItems) {
        this.callStackItems = callStackItems;
    }
}
