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

import com.google.inject.AbstractModule;
import io.etrace.agent.config.CollectorRegistry;
import io.etrace.agent.config.DefaultConfigManager;
import io.etrace.agent.io.MessageSender;
import io.etrace.agent.io.TcpMessageSender;
import io.etrace.agent.message.RequestIdAndRpcIdFactory;
import io.etrace.agent.message.callstack.CallstackProducer;
import io.etrace.agent.message.callstack.CallstackQueue;
import io.etrace.agent.message.manager.DefaultMessageManager;
import io.etrace.agent.message.metric.DefaultMetricManager;
import io.etrace.agent.message.metric.MetricProducer;
import io.etrace.agent.message.metric.MetricQueue;
import io.etrace.agent.stat.CallstackStats;
import io.etrace.agent.stat.MetricStats;
import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.metric.MetricManager;
import io.etrace.common.message.trace.TraceManager;
import io.etrace.common.util.NetworkInterfaceHelper;

import static com.google.inject.name.Names.named;

/**
 * Agent guice model
 */
public class AgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CallstackProducer.class).asEagerSingleton();
        bind(MetricProducer.class).asEagerSingleton();
        bind(TraceManager.class).to(DefaultMessageManager.class).asEagerSingleton();
        bind(MetricManager.class).to(DefaultMetricManager.class).asEagerSingleton();
        bind(CallstackQueue.class).asEagerSingleton();
        bind(MetricQueue.class).asEagerSingleton();

        // collector configs
        ConfigManger configManger = new DefaultConfigManager();
        bind(ConfigManger.class).toInstance(configManger);
        CollectorRegistry.getInstance().setConfigManger(configManger);

        // message
        CallstackStats callstackStats = new CallstackStats();
        bind(CallstackStats.class).toInstance(callstackStats);
        bind(MessageSender.class).toInstance(
            new TcpMessageSender(TcpMessageSender.TRACE, callstackStats.getTcpStats()));
        // metrics
        MetricStats metricStats = new MetricStats();
        bind(MetricStats.class).toInstance(metricStats);

        TcpMessageSender metricSender = new TcpMessageSender(TcpMessageSender.Metric, metricStats.getTcpStats());

        bind(MessageSender.class).annotatedWith(named(TcpMessageSender.METRIC_TCP_MESSAGE_SENDER)).toInstance(
            metricSender);

        bind(RequestIdAndRpcIdFactory.class).asEagerSingleton();

        bindConstant().annotatedWith(named("hostName")).to(NetworkInterfaceHelper.INSTANCE.getLocalHostName());
        bindConstant().annotatedWith(named("hostIp")).to(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress());
    }
}
