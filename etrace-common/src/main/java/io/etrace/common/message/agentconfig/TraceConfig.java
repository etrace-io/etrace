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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TraceConfig {
    private long id;
    private String configKey;
    private boolean enabled = true;
    private boolean aopEnabled = true;
    private int tagCount;
    private int tagSize;
    private int dataSize;
    private boolean longConnection = true;
    /**
     * default callstack message size is 500. If messages overflow, will truncate them.
     */
    private int messageCount = 500;
    private int redisSize;

    public TraceConfig(String configKey, boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize) {
        this(configKey, enabled, aopEnabled, tagCount, tagSize, dataSize, true);
    }

    public TraceConfig(String configKey, boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize,
                       boolean longConnection, int messageCount) {
        this.configKey = configKey;
        this.enabled = enabled;
        this.aopEnabled = aopEnabled;
        this.tagCount = tagCount;
        this.tagSize = tagSize;
        this.dataSize = dataSize;
        this.longConnection = longConnection;
        this.messageCount = messageCount;
    }

    public TraceConfig(String configKey, boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize,
                       boolean longConnection) {
        this.configKey = configKey;
        this.enabled = enabled;
        this.aopEnabled = aopEnabled;
        this.tagCount = tagCount;
        this.tagSize = tagSize;
        this.dataSize = dataSize;
        this.longConnection = longConnection;
    }

    public TraceConfig(boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize) {
        this(null, enabled, aopEnabled, tagCount, tagSize, dataSize, true);
    }

    public TraceConfig(boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize,
                       boolean longConnection) {
        this(null, enabled, aopEnabled, tagCount, tagSize, dataSize, longConnection);
    }
}
