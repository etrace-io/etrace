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

package io.etrace.agent.stat;

import java.util.concurrent.atomic.AtomicLong;

public class HeartbeatStats {
    private AtomicLong totalSize = new AtomicLong(0);
    private AtomicLong totalCount = new AtomicLong(0);
    private AtomicLong successCount = new AtomicLong(0);

    public void incTotalSize(long totalSize) {
        this.totalSize.set(totalSize);
    }

    public void incTotalCount(long totalCount) {
        this.totalCount.set(totalCount);
    }

    public void incSuccessCount(long successCount) {
        this.successCount.set(successCount);
    }

    public long getTotalSize() {
        return totalSize.get();
    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public HeartbeatStats copyStats() {
        HeartbeatStats newHeartbeatStats = new HeartbeatStats();
        newHeartbeatStats.incTotalSize(totalSize.get());
        newHeartbeatStats.incTotalCount(totalCount.get());
        newHeartbeatStats.incSuccessCount(successCount.get());
        return newHeartbeatStats;
    }

    public void decrement(HeartbeatStats heartbeatStats) {
        update(heartbeatStats, -1);
    }

    public void inc(HeartbeatStats heartbeatStats) {
        update(heartbeatStats, 1);
    }

    private void update(HeartbeatStats heartbeatStats, int sign) {
        totalSize.addAndGet(heartbeatStats.totalSize.get() * sign);
        totalCount.addAndGet(heartbeatStats.totalCount.get() * sign);
        successCount.addAndGet(heartbeatStats.successCount.get() * sign);
    }

    @Override
    public String toString() {
        return "HeartbeatStats{" +
            "totalSize=" + totalSize +
            ", totalCount=" + totalCount +
            ", successCount=" + successCount +
            '}';
    }
}
