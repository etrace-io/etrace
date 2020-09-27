/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        //init
        operatingSystemMXBean.getProcessCpuLoad();
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

        private double getProcessCpuLoad() {
            return sunOperatingSystemMXBean.getProcessCpuLoad();
        }

        private double getTotalPhysicalMemorySize() {
            return sunOperatingSystemMXBean.getTotalPhysicalMemorySize();
        }
    }
}
