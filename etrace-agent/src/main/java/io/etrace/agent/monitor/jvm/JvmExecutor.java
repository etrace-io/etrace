package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.Executor;
import io.etrace.agent.monitor.SunManagementBean;

import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.etrace.agent.monitor.jvm.JvmMetric.MetricType.*;

public class JvmExecutor extends Executor {
    private Map<String, GarbageInfo> garbageInfos = new HashMap<>();
    private SunManagementBean sunManagementBean = SunManagementBean.getBean();

    public JvmExecutor(String type) {
        super(type);
    }

    @Override
    public Map<String, String> execute() {
        JvmMetric jvmMetric = new JvmMetric(type);
        buildGarbageMetrics(jvmMetric);
        buildMemoryMetrics(jvmMetric);
        buildMemoryPoolMetrics(jvmMetric);
        buildThreadSizeMetrics(jvmMetric);
        buildClassLoadMetrics(jvmMetric);
        buildCpuMetrics(jvmMetric);
        return jvmMetric.getMetrics();
    }

    private void buildGarbageMetrics(JvmMetric metrics) {
        for (GarbageCollectorMXBean mbean : ManagementFactory.getGarbageCollectorMXBeans()) {
            GarbageInfo garbageInfo = getGarbageInfo(mbean.getName());
            long gcCount = mbean.getCollectionCount();
            if (gcCount > 0) {
                metrics.put(garbageInfo.getGcCountKey(), garbageInfo.getGcCount(gcCount));
            }

            long gcTime = mbean.getCollectionTime();
            if (gcTime > 0) {
                metrics.put(garbageInfo.getGcTimeKey(), garbageInfo.getGcTime(gcTime));
            }
        }
    }

    private GarbageInfo getGarbageInfo(String garbageName) {
        GarbageInfo garbageInfo = garbageInfos.get(garbageName);
        if (null == garbageInfo) {
            garbageInfo = new GarbageInfo(type, garbageName);
            garbageInfos.put(garbageName, garbageInfo);
        }
        return garbageInfo;
    }

    private void buildMemoryMetrics(JvmMetric metrics) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        metrics.put(MEMORY_HEAPUSED, memoryBean.getHeapMemoryUsage().getUsed());
        //        addValue(MetricType.MEMORY_HEAPCOMMITTED, memoryBean.getHeapMemoryUsage().getCommitted());
        metrics.put(MEMORY_HEAPMAX, memoryBean.getHeapMemoryUsage().getMax());

        metrics.put(MEMORY_NONHEAPUSED, memoryBean.getNonHeapMemoryUsage().getUsed());
    }

    private void buildMemoryPoolMetrics(JvmMetric metrics) {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String name = memoryPoolMXBean.getName();
            MemoryUsage usage = memoryPoolMXBean.getUsage();
            //                putAttrToMap(heapInfos, type, subType, "init", usage.getInit());
            metrics.put(MEMORY_POOL, name, "used", usage.getUsed());
            metrics.put(MEMORY_POOL, name, "committed", usage.getCommitted());
            metrics.put(MEMORY_POOL, name, "max", usage.getMax());
        }
    }

    public void buildThreadSizeMetrics(JvmMetric metrics) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        metrics.put(THREAD_THREADS, threadBean.getThreadCount());
        metrics.put(THREAD_DAEMON, threadBean.getDaemonThreadCount());

        // TODO check this more rarely
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        metrics.put(THREAD_DEADLOCKED, deadlockedThreads != null ? deadlockedThreads.length : 0);
    }

    public void buildClassLoadMetrics(JvmMetric metrics) {
        metrics.put(LOADED_CLASSES, ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
    }

    private void buildCpuMetrics(JvmMetric metrics) {
        metrics.put(CPU_USAGE, sunManagementBean.getProcessCpuLoad());
    }

}
