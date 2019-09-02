package io.etrace.agent.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class TCPStats {
    private AtomicLong totalSize = new AtomicLong(0);
    private AtomicLong tcpLoss = new AtomicLong(0);
    private AtomicLong lossInNet = new AtomicLong(0);
    private AtomicLong lossSize = new AtomicLong(0);
    private AtomicLong tcpPollCount = new AtomicLong(0);
    private AtomicLong successCount = new AtomicLong(0);
    private AtomicLong timeoutCount = new AtomicLong(0);

    public void incTotalSize(int totalSize) {
        this.totalSize.addAndGet(totalSize);
    }

    public void incTcpLoss(int loss) {
        this.tcpLoss.addAndGet(loss);
    }

    public void incLossSize(int lossSize) {
        this.lossSize.addAndGet(lossSize);
    }

    public void incTcpPollCount(int tcpPollCount) {
        this.tcpPollCount.addAndGet(tcpPollCount);
    }

    public void incLossInNet(int lossInNet) {
        this.lossInNet.addAndGet(lossInNet);
    }

    public void incSuccessCount(int count) {
        this.successCount.addAndGet(count);
    }

    public void incTimeoutCount(int count) {
        this.timeoutCount.addAndGet(count);
    }

    public long getTotalSize() {
        return totalSize.get();
    }

    public long getTcpLoss() {
        return tcpLoss.get();
    }

    public long getLossInNet() {
        return lossInNet.get();
    }

    public long getLossSize() {
        return lossSize.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public long getTcpPollCount() {
        return tcpPollCount.get();
    }

    public long getTimeoutCount() {
        return timeoutCount.get();
    }

    public TCPStats copyStats() {
        TCPStats TCPStats = new TCPStats();
        TCPStats.totalSize.addAndGet(getTotalSize());
        TCPStats.tcpLoss.addAndGet(getTcpLoss());
        TCPStats.lossInNet.addAndGet(getLossInNet());
        TCPStats.lossSize.addAndGet(getLossSize());
        TCPStats.tcpPollCount.addAndGet(getTcpPollCount());
        TCPStats.successCount.addAndGet(getSuccessCount());
        TCPStats.timeoutCount.addAndGet(getTimeoutCount());
        return TCPStats;
    }

    public void decrement(TCPStats tcpStats) {
        update(tcpStats, -1);
    }

    public void inc(TCPStats tcpStats) {
        update(tcpStats, 1);
    }

    private void update(TCPStats tcpStats, int sign) {
        totalSize.addAndGet(tcpStats.totalSize.get() * sign);
        tcpLoss.addAndGet(tcpStats.tcpLoss.get() * sign);
        lossInNet.addAndGet(tcpStats.lossInNet.get() * sign);
        lossSize.addAndGet(tcpStats.lossSize.get() * sign);
        tcpPollCount.addAndGet(tcpStats.tcpPollCount.get() * sign);
        successCount.addAndGet(tcpStats.successCount.get() * sign);
        timeoutCount.addAndGet(tcpStats.timeoutCount.get() * sign);
    }

    public Map<String, Object> toStatMap() {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalSize", getTotalSize());
        statsMap.put("tcpLoss", getTcpLoss());
        statsMap.put("lossInNet", getLossInNet());
        statsMap.put("lossSize", getLossSize());
        statsMap.put("tcpPollCount", getTcpPollCount());
        statsMap.put("successCount", getSuccessCount());
        statsMap.put("timeoutCount", getTimeoutCount());
        return statsMap;
    }

    @Override
    public String toString() {
        return "TCPStats{" +
            "totalSize=" + totalSize +
            ", tcpLoss=" + tcpLoss +
            ", lossInNet=" + lossInNet +
            ", lossSize=" + lossSize +
            ", tcpPollCount=" + tcpPollCount +
            ", successCount=" + successCount +
            ", timeoutCount=" + timeoutCount +
            '}';
    }
}
