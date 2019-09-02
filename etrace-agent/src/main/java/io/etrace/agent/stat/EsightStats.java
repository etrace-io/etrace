package io.etrace.agent.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class EsightStats {
    private AtomicLong totalCount = new AtomicLong(0);
    private AtomicLong lossCount = new AtomicLong(0);
    private AtomicLong totalPackageCount = new AtomicLong(0);
    private AtomicLong lossPackageCount = new AtomicLong(0);

    private AtomicLong totalPackageSize = new AtomicLong(0);
    private AtomicLong lossPackageSize = new AtomicLong(0);

    private TCPStats tcpStats = new TCPStats();

    public void increaseCount(int count) {
        totalCount.addAndGet(count);
    }

    public void increaseLossCount(int count) {
        lossCount.addAndGet(count);
    }

    public void increasePackageCount(int count) {
        totalPackageCount.addAndGet(count);
    }

    public void increaseLossPackageCount(int count) {
        lossPackageCount.addAndGet(count);
    }

    public void increasePackageSize(int count) {
        totalPackageSize.addAndGet(count);
    }

    public void increaseLossPackageSize(int count) {
        lossPackageSize.addAndGet(count);
    }

    public AtomicLong getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(AtomicLong totalCount) {
        this.totalCount = totalCount;
    }

    public AtomicLong getLossCount() {
        return lossCount;
    }

    public void setLossCount(AtomicLong lossCount) {
        this.lossCount = lossCount;
    }

    public AtomicLong getTotalPackageCount() {
        return totalPackageCount;
    }

    public void setTotalPackageCount(AtomicLong totalPackageCount) {
        this.totalPackageCount = totalPackageCount;
    }

    public AtomicLong getLossPackageCount() {
        return lossPackageCount;
    }

    public void setLossPackageCount(AtomicLong lossPackageCount) {
        this.lossPackageCount = lossPackageCount;
    }

    public AtomicLong getTotalPackageSize() {
        return totalPackageSize;
    }

    public void setTotalPackageSize(AtomicLong totalPackageSize) {
        this.totalPackageSize = totalPackageSize;
    }

    public AtomicLong getLossPackageSize() {
        return lossPackageSize;
    }

    public void setLossPackageSize(AtomicLong lossPackageSize) {
        this.lossPackageSize = lossPackageSize;
    }

    public TCPStats getTcpStats() {
        return tcpStats;
    }

    public void setTcpStats(TCPStats tcpStats) {
        this.tcpStats = tcpStats;
    }

    public Map<String, Object> drainToMap() {
        Map<String, Object> statsMap = new HashMap<>(6);
        statsMap.put("esight_totalCount", totalCount.getAndSet(0));
        statsMap.put("esight_lossCount", lossCount.getAndSet(0));
        statsMap.put("esight_totalPackageCount", totalPackageCount.getAndSet(0));
        statsMap.put("esight_lossPackageCount", lossPackageCount.getAndSet(0));
        statsMap.put("esight_totalPackageSize", totalPackageSize.getAndSet(0));
        statsMap.put("esight_lossPackageSize", lossPackageSize.getAndSet(0));
        if (tcpStats != null) {
            statsMap.putAll(tcpStats.toStatMap());
            tcpStats.decrement(tcpStats);  // -_- !!
        }
        return statsMap;
    }
}
