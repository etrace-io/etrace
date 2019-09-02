package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.HBConstants;

import static io.etrace.agent.monitor.jvm.JvmMetric.MetricType.GARBAGE_COUNT;
import static io.etrace.agent.monitor.jvm.JvmMetric.MetricType.GARBAGE_TIME;

public class GarbageInfo {
    private final String gcCountKey;
    private final String gcTimeKey;

    private long gcCount = -1l;
    private long gcTime = -1l;

    public GarbageInfo(String type, String garbageName) {
        this.gcCountKey = type + HBConstants.TYPE_DELIMIT + GARBAGE_COUNT.toString().replace("_",
            HBConstants.TYPE_DELIMIT).toLowerCase() + HBConstants.TYPE_DELIMIT + garbageName;
        this.gcTimeKey = type + HBConstants.TYPE_DELIMIT + GARBAGE_TIME.toString().replace("_",
            HBConstants.TYPE_DELIMIT).toLowerCase() + HBConstants.TYPE_DELIMIT + garbageName;
    }

    public String getGcCountKey() {
        return gcCountKey;
    }

    public String getGcTimeKey() {
        return gcTimeKey;
    }

    public long getGcCount(long gcCount) {
        long count;
        if (this.gcCount > 0) {
            count = gcCount - this.gcCount;
        } else {
            count = gcCount;
        }
        this.gcCount = gcCount;
        return count;
    }

    public long getGcTime(long gcTime) {
        long time;
        if (this.gcTime > 0) {
            time = gcTime - this.gcTime;
        } else {
            time = gcTime;
        }
        this.gcTime = gcTime;
        return time;
    }
}
