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

package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.HeartBeatConstants;

import static io.etrace.agent.monitor.jvm.JvmMetric.JVMMetricType.GARBAGE_COUNT;
import static io.etrace.agent.monitor.jvm.JvmMetric.JVMMetricType.GARBAGE_TIME;

public class GarbageInfo {
    private final String gcCountKey;
    private final String gcTimeKey;

    private long gcCount = -1L;
    private long gcTime = -1L;

    public GarbageInfo(String type, String garbageName) {
        this.gcCountKey = type + HeartBeatConstants.TYPE_DELIMIT + GARBAGE_COUNT.toString().replace("_",
            HeartBeatConstants.TYPE_DELIMIT).toLowerCase() + HeartBeatConstants.TYPE_DELIMIT + garbageName;
        this.gcTimeKey = type + HeartBeatConstants.TYPE_DELIMIT + GARBAGE_TIME.toString().replace("_",
            HeartBeatConstants.TYPE_DELIMIT).toLowerCase() + HeartBeatConstants.TYPE_DELIMIT + garbageName;
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
