package io.etrace.agent;

import com.google.inject.AbstractModule;
import io.etrace.agent.config.CollectorRegistry;
import io.etrace.agent.config.DefaultConfigManager;
import io.etrace.agent.message.CallStackProducer;
import io.etrace.agent.message.IdFactory;
import io.etrace.agent.message.MessageProducer;
import io.etrace.agent.message.io.MessageSender;
import io.etrace.agent.message.io.TcpMessageSender;
import io.etrace.agent.message.manager.DefaultMessageManager;
import io.etrace.agent.message.metric.DefaultMetricManager;
import io.etrace.agent.message.metric.MetricProducer;
import io.etrace.agent.message.metric.MetricQueue;
import io.etrace.agent.stat.EsightStats;
import io.etrace.agent.stat.MessageStats;
import io.etrace.agent.stat.MetricStats;
import io.etrace.common.message.ConfigManger;
import io.etrace.common.message.MessageManager;
import io.etrace.common.message.MetricManager;
import io.etrace.common.util.NetworkInterfaceHelper;

import static com.google.inject.name.Names.named;

/**
 * Agent guice model
 */
public class AgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MessageProducer.class).asEagerSingleton();
        bind(MetricProducer.class).asEagerSingleton();
        bind(MessageManager.class).to(DefaultMessageManager.class).asEagerSingleton();
        bind(MetricManager.class).to(DefaultMetricManager.class).asEagerSingleton();
        bind(CallStackProducer.class).asEagerSingleton();
        bind(MetricQueue.class).asEagerSingleton();

        // collector configs
        ConfigManger configManger = new DefaultConfigManager();
        bind(ConfigManger.class).toInstance(configManger);
        CollectorRegistry.getInstance().setConfigManger(configManger);

        // message
        MessageStats messageStats = new MessageStats();
        bind(MessageStats.class).toInstance(messageStats);
        bind(MessageSender.class).toInstance(new TcpMessageSender("Trace", messageStats.getTcpStats()));

        // metrics
        MetricStats metricStats = new MetricStats();
        bind(MetricStats.class).toInstance(metricStats);

        TcpMessageSender metricSender = new TcpMessageSender("Metric", metricStats.getTcpStats());
        bind(MessageSender.class).annotatedWith(named("metricTCPMessageSender")).toInstance(metricSender);

        // esight
        EsightStats esightStats = new EsightStats();
        bind(EsightStats.class).toInstance(esightStats);
        TcpMessageSender esightSender = new TcpMessageSender("Esight", esightStats.getTcpStats());
        bind(MessageSender.class).annotatedWith(named("esightTCPMessageSender")).toInstance(esightSender);

        bind(IdFactory.class).asEagerSingleton();

        bindConstant().annotatedWith(named("hostName")).to(NetworkInterfaceHelper.INSTANCE.getLocalHostName());
        bindConstant().annotatedWith(named("hostIp")).to(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress());
    }
}
