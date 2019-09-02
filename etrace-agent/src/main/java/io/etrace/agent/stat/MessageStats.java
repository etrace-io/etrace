package io.etrace.agent.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MessageStats {
    private AtomicLong totalCount = new AtomicLong(0);
    private AtomicLong loss = new AtomicLong(0);
    private TCPStats tcpStats;
    private HeartbeatStats heartbeatStats;
    private MessageStats history;

    public MessageStats(TCPStats tcpStats, HeartbeatStats heartbeatStats) {
        this.tcpStats = tcpStats;
        this.heartbeatStats = heartbeatStats;
    }

    public MessageStats() {
        tcpStats = new TCPStats();
        heartbeatStats = new HeartbeatStats();
    }

    public TCPStats getTcpStats() {
        return tcpStats;
    }

    public void incTotalCount() {
        this.totalCount.addAndGet(1);
    }

    public void incLoss() {
        this.loss.addAndGet(1);
    }

    public void incLoss(int loss) {
        this.loss.addAndGet(loss);
    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public void setTotalCount(long totalCount) {
        this.totalCount.set(totalCount);
    }

    public long getLoss() {
        return loss.get();
    }

    public void setLoss(long loss) {
        this.loss.set(loss);
    }

    public HeartbeatStats getHeartbeatStats() {
        return heartbeatStats;
    }

    public MessageStats getHistory() {
        if (history == null) {
            history = new MessageStats();
        }
        return history;
    }

    public MessageStats copyStats() {
        TCPStats newTCPStats = this.tcpStats.copyStats();
        HeartbeatStats newHeartbeatStats = this.heartbeatStats.copyStats();
        MessageStats newStats = new MessageStats(newTCPStats, newHeartbeatStats);
        newStats.setLoss(this.getLoss());
        newStats.setTotalCount(this.getTotalCount());
        return newStats;
    }

    public Map<String, Object> toStatMap() {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("lossSize", tcpStats.getLossSize());
        statsMap.put("loss", this.getLoss() + tcpStats.getTcpLoss());
        statsMap.put("lossInNet", tcpStats.getLossInNet());
        statsMap.put("totalSize", tcpStats.getTotalSize() + this.heartbeatStats.getTotalSize());
        statsMap.put("totalCount", this.getTotalCount() + this.heartbeatStats.getTotalCount());
        statsMap.put("successCount", tcpStats.getSuccessCount() + this.heartbeatStats.getSuccessCount());
        statsMap.put("tcpPollCount", tcpStats.getTcpPollCount());

        return statsMap;
    }

    public void resetToHistory(MessageStats resetStats) {
        this.loss.addAndGet(-resetStats.getLoss());
        this.totalCount.addAndGet(-resetStats.getTotalCount());
        this.tcpStats.decrement(resetStats.tcpStats);
        this.heartbeatStats.decrement(resetStats.heartbeatStats);
        getHistory().tcpStats.inc(resetStats.tcpStats);
        getHistory().loss.addAndGet(resetStats.getLoss());
        getHistory().totalCount.addAndGet(resetStats.getTotalCount());
        getHistory().heartbeatStats.inc(resetStats.heartbeatStats);
    }

    @Override
    public String toString() {
        return "MessageStats{" +
            "totalCount=" + totalCount +
            ", loss=" + loss +
            ", tcpStats=" + tcpStats +
            ", heartbeatStats=" + heartbeatStats +
            ", history=" + history +
            '}';
    }
}
