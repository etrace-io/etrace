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

public interface ConfigManger {

    TraceConfig DEFAULT_AGENT_CONFIOG = new TraceConfig(true, true, 12, 256, 2 * 1024, true);

    MetricConfig DEFAULT_METRIC_CONFIG = new MetricConfig(true, 8, 256, 1000, 100, 10000, 1000, 1000);

    void init();

    void shutdown();

    MetricConfig getMetricConfig();

    TraceConfig getAgentConfig();

    boolean isEnabled();

    boolean isAopEnabled();

    int getTagCount();

    int getTagSize();

    int getDataSize();

    int getMessageCount();

    int getRedisSize();
}
