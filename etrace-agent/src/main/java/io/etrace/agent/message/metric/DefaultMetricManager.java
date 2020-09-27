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

package io.etrace.agent.message.metric;

import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.MetricManager;

public class DefaultMetricManager implements MetricManager {
    private MetricQueue metricQueue;
    private ConfigManger configManger;

    @Inject
    public DefaultMetricManager(MetricQueue metricQueue, ConfigManger configManger) {
        this.metricQueue = metricQueue;
        this.configManger = configManger;
    }

    @Override
    public ConfigManger getConfigManager() {
        return configManger;
    }

    @Override
    public void addMetric(MetricInTraceApi metricInTraceApi) {
        metricQueue.produce(metricInTraceApi);
    }

    @Override
    public String getAppId() {
        return AgentConfiguration.getAppId();
    }
}
