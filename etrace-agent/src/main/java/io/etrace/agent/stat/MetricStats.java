package io.etrace.agent.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MetricStats {
    private AtomicLong totalCount = new AtomicLong(0);
    private AtomicLong loss = new AtomicLong(0);
    private AtomicLong packageLoss = new AtomicLong(0);
    private AtomicLong merge = new AtomicLong(0);
    private AtomicLong mergeAfterTotal = new AtomicLong(0);
    private TCPStats tcpStats;
    private MetricStats history;

    public MetricStats() {
        tcpStats = new TCPStats();
    }

    public MetricStats(TCPStats tcpStats) {
        this.tcpStats = tcpStats;
    }

    public void incMergeAfterTotal(long count) {
        mergeAfterTotal.addAndGet(count);
    }

    public void incTotalCount() {
        this.totalCount.addAndGet(1);
    }

    public void incLoss() {
        this.loss.addAndGet(1);
    }

    public void incPackageLoss(int count) {
        packageLoss.addAndGet(count);
    }

    public void incMerge(int count) {
        this.merge.addAndGet(count);
    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public void setTotalCount(long totalCount) {
        this.totalCount.addAndGet(totalCount);
    }

    public long getLoss() {
        return loss.get();
    }

    public void setLoss(long loss) {
        this.loss.addAndGet(loss);
    }

    public long getMerge() {
        return merge.get();
    }

    public void setMerge(long merge) {
        this.merge.addAndGet(merge);
    }

    public long getMergeAfterTotal() {
        return mergeAfterTotal.get();
    }

    public long getPackageLoss() {
        return packageLoss.get();
    }

    public TCPStats getTcpStats() {
        return tcpStats;
    }

    public MetricStats getHistory() {
        if (history == null) {
            history = new MetricStats();
        }
        return history;
    }

    public Map<String, Object> toStatMap() {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalCount", getTotalCount());
        statsMap.put("loss", getLoss());
        statsMap.put("packageLoss", getPackageLoss());
        statsMap.put("merge", getMerge());
        statsMap.put("mergeAfterTotal", getMergeAfterTotal());
        statsMap.putAll(tcpStats.toStatMap());
        return statsMap;
    }

    public MetricStats copyStats() {
        TCPStats newTCPStats = this.tcpStats.copyStats();
        MetricStats newStats = new MetricStats(newTCPStats);
        newStats.loss.addAndGet(getLoss());
        newStats.totalCount.addAndGet(getTotalCount());
        newStats.packageLoss.addAndGet(getPackageLoss());
        newStats.merge.addAndGet(getMerge());
        newStats.mergeAfterTotal.addAndGet(getMergeAfterTotal());
        return newStats;
    }

    public void resetToHistory(MetricStats resetStats) {
        this.loss.addAndGet(-resetStats.getLoss());
        this.totalCount.addAndGet(-resetStats.getTotalCount());
        this.packageLoss.addAndGet(-resetStats.getPackageLoss());
        this.merge.addAndGet(-resetStats.getMerge());
        this.mergeAfterTotal.addAndGet(-resetStats.getMergeAfterTotal());
        this.tcpStats.decrement(resetStats.tcpStats);
        getHistory().loss.addAndGet(resetStats.getLoss());
        getHistory().totalCount.addAndGet(resetStats.getTotalCount());
        getHistory().packageLoss.addAndGet(resetStats.getPackageLoss());
        getHistory().merge.addAndGet(resetStats.getMerge());
        getHistory().mergeAfterTotal.addAndGet(resetStats.getMergeAfterTotal());
        getHistory().tcpStats.inc(resetStats.tcpStats);
    }

    @Override
    public String toString() {
        return "MetricStats{" +
            "totalCount=" + totalCount +
            ", loss=" + loss +
            ", packageLoss=" + packageLoss +
            ", merge=" + merge +
            ", mergeAfterTotal=" + mergeAfterTotal +
            ", tcpStats=" + tcpStats +
            ", history=" + history +
            '}';
    }
}
