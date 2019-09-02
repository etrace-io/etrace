package io.etrace.agent.config;

import com.google.common.base.Strings;
import io.etrace.common.message.ConfigManger;
import io.etrace.common.modal.AgentConfig;
import io.etrace.common.modal.CollectorItem;
import io.etrace.common.modal.metric.MetricConfig;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.NetworkInterfaceHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultConfigManager implements ConfigManger {
    /**
     * default is 12 tags for callstack
     **/
    private volatile AgentConfig agentConfig = new AgentConfig(true, true, 12, 256, 2 * 1024, true);
    /**
     * default is 8 tags for metrics
     */
    private volatile MetricConfig metricConfig = new MetricConfig(true, 8, 256, 1000, 100, 10000, 1000, 1000);

    private Timer timer;

    public DefaultConfigManager() {
        //after timer -- get config per interval
        timer = new Timer("Config-Fetch-Timer", true);
        int pullIntervalSeconds = 60;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                init();
            }
        }, 0, pullIntervalSeconds * 1000);
    }

    @Override
    public void init() {
        pullMetricConfig();
        pullAgentConfig();
        pullCollectorList();
    }

    @Override
    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public MetricConfig getMetricConfig() {
        return this.metricConfig;
    }

    @Override
    public boolean isEnabled() {
        return agentConfig.isEnabled() && !Strings.isNullOrEmpty(AgentConfiguration.getCollectorIp())
            && CollectorRegistry.getInstance().isAvailable();
    }

    @Override
    public boolean isAopEnabled() {
        return agentConfig.isAopEnabled();
    }

    @Override
    public int getTagCount() {
        return agentConfig.getTagCount();
    }

    @Override
    public int getTagSize() {
        return agentConfig.getTagSize();
    }

    @Override
    public int getDataSize() {
        return agentConfig.getDataSize();
    }

    @Override
    public int getMessageCount() {
        if (agentConfig.getMessageCount() <= 0) {
            return 500;
        } else {
            return agentConfig.getMessageCount();
        }
    }

    @Override
    public int getRedisSize() {
        if (agentConfig.getRedisSize() <= 0) {
            return 500;
        }
        return agentConfig.getRedisSize();
    }

    public void pullMetricConfig() {
        if (Strings.isNullOrEmpty(AgentConfiguration.getCollectorIp())) {
            return;
        }
        String url = getMetricConfigUrl();
        String configJson = getConfigFromHttp(url);
        if (Strings.isNullOrEmpty(configJson)) {
            try {
                metricConfig = JSONUtil.toObject(configJson, MetricConfig.class);
            } catch (IOException ignore) {
            }
        }
    }

    public void pullAgentConfig() {
        try {
            if (Strings.isNullOrEmpty(AgentConfiguration.getCollectorIp())) {
                return;
            }
            String collectorUrl = getCollectorHttpAgentConfigUrl();
            String configJson = getConfigFromHttp(collectorUrl);
            if (!Strings.isNullOrEmpty(configJson)) {
                try {
                    agentConfig = JSONUtil.toObject(configJson, AgentConfig.class);
                    CollectorRegistry.getInstance().setLongConnection(agentConfig.isLongConnection());
                } catch (IOException ignore) {
                }
            }
        } catch (Throwable e) {
            // ignore //
        }
    }

    public void pullCollectorList() {
        if (Strings.isNullOrEmpty(AgentConfiguration.getCollectorIp())) {
            return;
        }
        try {
            String collectorUrl = getCollectorAddressUrl();
            String configJson = getConfigFromHttp(collectorUrl);
            if (Strings.isNullOrEmpty(configJson)) {
                CollectorItem item;
                item = JSONUtil.toObject(configJson, CollectorItem.class);
                CollectorRegistry.getInstance().setCollectorItem(item);
            }
            // if fail to pullCollectorList
            // should not clean remaining collectorList. 
            // still using them, util next pulling.
            //  else {
            //      CollectorRegistry.getInstance().clearCollectors();
            //  }
        } catch (Throwable e) {
            // if fail to pullCollectorList
            // should not clean remaining collectorList. 
            // still using them, util next pulling.
            // CollectorRegistry.getInstance().clearCollectors();
        } finally {
            if (CollectorRegistry.getInstance().getCollectorsSize() < 1) {
                CollectorRegistry.getInstance().setIsAvailable(false);
            } else {
                CollectorRegistry.getInstance().setIsAvailable(true);
            }
        }
    }

    private String getConfigFromHttp(String url) {
        InputStream contentInput = null;
        try {
            URL onlineUrl = new URL(url);
            URLConnection onlineConn = onlineUrl.openConnection();
            onlineConn.setConnectTimeout(1000 * 2);
            onlineConn.setReadTimeout(1000 * 2);
            contentInput = onlineConn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(contentInput));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String configJson = sb.toString();
            contentInput.close();
            contentInput = null;
            return configJson;
        } catch (IOException ignored) {
        } finally {
            if (contentInput != null) {
                try {
                    contentInput.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private String getCollectorHttpAgentConfigUrl() {
        return String.format("http://%s:%d/agent-config?appId=%s&host=%s&hostName=%s",
            AgentConfiguration.getCollectorIp(),
            AgentConfiguration.getCollectorPort(), AgentConfiguration.getServiceName(),
            NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(), NetworkInterfaceHelper.INSTANCE.getLocalHostName());
    }

    private String getCollectorAddressUrl() {
        return String.format("http://%s:%d/collector/item?appId=%s&host=%s&hostName=%s",
            AgentConfiguration.getCollectorIp(), AgentConfiguration.getCollectorPort(),
            AgentConfiguration.getServiceName(), NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(),
            NetworkInterfaceHelper.INSTANCE.getLocalHostName());
    }

    private String getMetricConfigUrl() {
        return String.format("http://%s:%d/metric-config?appId=%s&host=%s&hostName=%s",
            AgentConfiguration.getCollectorIp(), AgentConfiguration.getCollectorPort(),
            AgentConfiguration.getServiceName(), NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(),
            NetworkInterfaceHelper.INSTANCE.getLocalHostName());
    }
}
