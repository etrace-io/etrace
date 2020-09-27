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

package io.etrace.agent.module;

import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.agentconfig.MetricConfig;
import io.etrace.common.message.agentconfig.TraceConfig;

public class TestConfigManger implements ConfigManger {
    @Override
    public void init() {

    }

    @Override
    public MetricConfig getMetricConfig() {
        return DEFAULT_METRIC_CONFIG;
    }

    @Override
    public TraceConfig getAgentConfig() {
        return DEFAULT_AGENT_CONFIOG;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAopEnabled() {
        return true;
    }

    @Override
    public int getTagCount() {
        return DEFAULT_AGENT_CONFIOG.getTagCount();
    }

    @Override
    public int getTagSize() {
        return DEFAULT_AGENT_CONFIOG.getTagSize();
    }

    @Override
    public int getDataSize() {
        return DEFAULT_AGENT_CONFIOG.getDataSize();
    }

    @Override
    public int getMessageCount() {
        return DEFAULT_AGENT_CONFIOG.getMessageCount();
    }

    @Override
    public int getRedisSize() {
        return DEFAULT_AGENT_CONFIOG.getRedisSize();
    }

    @Override
    public void shutdown() {

    }
}
