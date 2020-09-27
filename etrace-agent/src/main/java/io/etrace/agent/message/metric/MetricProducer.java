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
import io.etrace.common.message.metric.*;
import io.etrace.common.message.metric.impl.AbstractMetric;
import io.etrace.common.message.metric.impl.*;

import java.util.Map;

public class MetricProducer {
    private static Counter COUNTER_EMPTY = new DummyCounter();
    private static Gauge GAUGE_EMPTY = new DummyGauge();
    private static Timer TIMER_EMPTY = new DummyTimer();
    private static Payload PAYLOAD_EMPTY = new DummyPayload();

    private MetricManager metricManager;

    @Inject
    public MetricProducer(MetricManager metricManager) {
        this.metricManager = metricManager;
    }

    public Counter newCounter(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return COUNTER_EMPTY;
        }
        return addGlobalTags(new CounterImpl(metricManager, name));
    }

    public Gauge newGauge(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return GAUGE_EMPTY;
        }
        return addGlobalTags(new GaugeImpl(metricManager, name));
    }

    public Timer newTimer(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return TIMER_EMPTY;
        }
        return addGlobalTags(new TimerImpl(metricManager, name));
    }

    public Payload newPayload(String name) {
        ConfigManger configManger = metricManager.getConfigManager();
        if (!configManger.isEnabled() || !configManger.getMetricConfig().isEnabled()) {
            return PAYLOAD_EMPTY;
        }
        return addGlobalTags(new PayloadImpl(metricManager, name));
    }

    /**
     * want to add global tags into metrics, but here is the better place to add. because, once metrics 'completed', we
     * can't add tags any more!
     */
    private <T extends AbstractMetric> T addGlobalTags(T metric) {
        if (AgentConfiguration.getGlobalTags() != null) {
            for (Map.Entry<String, String> entry : AgentConfiguration.getGlobalTags().entrySet()) {
                metric.addTag(entry.getKey(), entry.getValue());
            }
        }
        return metric;
    }
}
