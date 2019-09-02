package io.etrace.common.modal;

import java.util.ArrayList;
import java.util.List;

public class CollectorItem {

    private List<Collector> thriftCollector;
    private List<Collector> tcpCollector;
    private boolean isUseTcp = false;

    public CollectorItem() {
    }

    public CollectorItem(int size) {
        tcpCollector = new ArrayList<>(size);
        thriftCollector = new ArrayList<>(size);
    }

    public List<Collector> getThriftCollector() {
        return thriftCollector;
    }

    public void setThriftCollector(List<Collector> thriftCollector) {
        this.thriftCollector = thriftCollector;
    }

    public List<Collector> getTcpCollector() {
        return tcpCollector;
    }

    public void setTcpCollector(List<Collector> tcpCollector) {
        this.tcpCollector = tcpCollector;
    }

    public boolean isUseTcp() {
        return isUseTcp;
    }

    public void setUseTcp(boolean useTcp) {
        isUseTcp = useTcp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        CollectorItem item = (CollectorItem)o;

        if (isUseTcp != item.isUseTcp) { return false; }
        if (!(tcpCollector.containsAll(item.tcpCollector) && item.tcpCollector.containsAll(tcpCollector))) {
            return false;
        }
        return thriftCollector.containsAll(item.thriftCollector) && item.thriftCollector.containsAll(thriftCollector);

    }

    @Override
    public int hashCode() {
        int result = thriftCollector.hashCode();
        result = 31 * result + tcpCollector.hashCode();
        result = 31 * result + (isUseTcp ? 1 : 0);
        return result;
    }
}
