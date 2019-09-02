package io.etrace.agent.monitor;

import java.lang.management.ManagementFactory;

public class SunManagementBean {
    private static SunManagementBean jvmSunManagementBean = new SunManagementBean();
    private OperatingSystemMXBean operatingSystemMXBean;

    private SunManagementBean() {
        try {
            Class.forName("com.sun.management.OperatingSystemMXBean");
        } catch (ClassNotFoundException e) {
            return;
        }
        operatingSystemMXBean = new OperatingSystemMXBean();
        operatingSystemMXBean.getProcessCpuLoad();    //init
    }

    public static SunManagementBean getBean() {
        return jvmSunManagementBean;
    }

    public double getProcessCpuLoad() {
        if (operatingSystemMXBean == null) {
            return -1D;
        }
        return operatingSystemMXBean.getProcessCpuLoad() * 100D;
    }

    public double getSystemMemorySize() {
        if (operatingSystemMXBean == null) {
            return -1D;
        }
        return operatingSystemMXBean.getTotalPhysicalMemorySize();
    }

    private class OperatingSystemMXBean {
        com.sun.management.OperatingSystemMXBean sunOperatingSystemMXBean = ManagementFactory.getPlatformMXBean(
            com.sun.management.OperatingSystemMXBean.class);

        public double getProcessCpuLoad() {
            return sunOperatingSystemMXBean.getProcessCpuLoad();
        }

        public double getTotalPhysicalMemorySize() {
            return sunOperatingSystemMXBean.getTotalPhysicalMemorySize();
        }
    }
}
