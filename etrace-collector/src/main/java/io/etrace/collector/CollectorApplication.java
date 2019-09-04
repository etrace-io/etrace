package io.etrace.collector;

import io.etrace.agent.Trace;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.collector.register.CollectorRegister;
import io.etrace.common.util.NetworkInterfaceHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }

    @PostConstruct
    public void postConstruct() {
        try {
            initEtraceAgent();
            registerCollector();
            Trace.logEvent("Collector", "Start");
            System.out.print("Collector started!");
        } catch (Throwable e) {
            System.out.print("Start collector error: ");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void initEtraceAgent() {
        AgentConfiguration.setInstance("instance-" + NetworkInterfaceHelper.INSTANCE.getLocalHostName());
        AgentConfiguration.setAppId("me.ele.arch.etrace.collector");
        AgentConfiguration.setTeam("etrace");
    }

    @Value("${zookeeper.address}")
    private String zookeeperAddress;

    @Value("${zookeeper.namespace}")
    private String zookeeperNamespace;

    @Value("${network.thrift.port}")
    private int thriftPort;
    @Value("${network.tcp.port}")
    private int tcpPort;
    @Autowired
    CollectorRegister register;

    private void registerCollector() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(zookeeperAddress).namespace(zookeeperNamespace)
            .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
            .connectionTimeoutMs(5000)
            .build();
        client.start();

        register.startup(client);

        //register thrift port
        register.register(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(), thriftPort);
        //register tcp port
        register.register(NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(), tcpPort);

    }
}
