/*
 * Copyright 2020 etrace.io
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

package io.etrace.common.message.agentconfig;

import lombok.Data;

@Data
public class MetricConfig {
    private boolean enabled = true;
    private int tagCount;
    private int tagSize;
    private int maxPackageCount;
    private int maxMetric;
    private int maxGroup;
    private int maxHistogramGroup;
    private int aggregatorTime;

    public MetricConfig() {
        this(true, 8, 256, 1000, 100, 10000, 1000, 1000);
    }

    public MetricConfig(boolean enabled, int tagCount, int tagSize, int maxPackageCount, int maxMetric, int maxGroup,
                        int maxHistogramGroup, int aggregatorTime) {
        this.enabled = enabled;
        this.tagCount = tagCount;
        this.tagSize = tagSize;
        this.maxPackageCount = maxPackageCount;
        this.maxMetric = maxMetric;
        this.maxGroup = maxGroup;
        this.maxHistogramGroup = maxHistogramGroup;
        this.aggregatorTime = aggregatorTime;
    }
}
