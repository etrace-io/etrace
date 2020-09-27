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

package io.etrace.agent.config;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etrace.common.message.agentconfig.CollectorItem;
import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.agentconfig.MetricConfig;
import io.etrace.common.message.agentconfig.TraceConfig;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.NetworkInterfaceHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultConfigManager implements ConfigManger {
    public static final long PULL_CONFIG_INTERVAL_IN_MILLISECOND = TimeUnit.MINUTES.toMillis(1);
    /**
     * default is 12 tags for callstack
     **/
    private volatile TraceConfig traceConfig = DEFAULT_AGENT_CONFIOG;
    /**
     * default is 8 tags for metrics
     */
    private volatile MetricConfig metricConfig = DEFAULT_METRIC_CONFIG;
    private ScheduledExecutorService executorService;

    public DefaultConfigManager() {
        executorService = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Config-Fetch-Timer-%d").build());

        executorService.scheduleAtFixedRate(this::init, 0, PULL_CONFIG_INTERVAL_IN_MILLISECOND, TimeUnit.MILLISECONDS);
    }

    @Override
    public void init() {
        pullMetricConfig();
        pullAgentConfig();
        pullCollectorList();
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public MetricConfig getMetricConfig() {
        return this.metricConfig;
    }

    @Override
    public TraceConfig getAgentConfig() {
        return this.traceConfig;
    }

    @Override
    public boolean isEnabled() {
        return traceConfig.isEnabled() && !Strings.isNullOrEmpty(AgentConfiguration.getCollectorIp())
            && CollectorRegistry.getInstance().isAvailable();
    }

    @Override
    public boolean isAopEnabled() {
        return traceConfig.isAopEnabled();
    }

    @Override
    public int getTagCount() {
        return traceConfig.getTagCount();
    }

    @Override
    public int getTagSize() {
        return traceConfig.getTagSize();
    }

    @Override
    public int getDataSize() {
        return traceConfig.getDataSize();
    }

    @Override
    public int getMessageCount() {
        return traceConfig.getMessageCount();
    }

    @Override
    public int getRedisSize() {
        if (traceConfig.getRedisSize() <= 0) {
            return 500;
        }
        return traceConfig.getRedisSize();
    }

    public void pullMetricConfig() {
        if (Strings.isNullOrEmpty(AgentConfiguration.getCollectorIp())) {
            return;
        }
        String url = getMetricConfigUrl();
        String configJson = getConfigFromHttp(url);
        if (!Strings.isNullOrEmpty(configJson)) {
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
                    traceConfig = JSONUtil.toObject(configJson, TraceConfig.class);
                    CollectorRegistry.getInstance().setLongConnection(traceConfig.isLongConnection());
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
            if (!Strings.isNullOrEmpty(configJson)) {
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
            AgentConfiguration.getCollectorPort(), AgentConfiguration.getAppId(),
            NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(), NetworkInterfaceHelper.INSTANCE.getLocalHostName());
    }

    private String getCollectorAddressUrl() {
        return String.format("http://%s:%d/collector/item?appId=%s&host=%s&hostName=%s",
            AgentConfiguration.getCollectorIp(), AgentConfiguration.getCollectorPort(),
            AgentConfiguration.getAppId(), NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(),
            NetworkInterfaceHelper.INSTANCE.getLocalHostName());
    }

    private String getMetricConfigUrl() {
        return String.format("http://%s:%d/metric-config?appId=%s&host=%s&hostName=%s",
            AgentConfiguration.getCollectorIp(), AgentConfiguration.getCollectorPort(),
            AgentConfiguration.getAppId(), NetworkInterfaceHelper.INSTANCE.getLocalHostAddress(),
            NetworkInterfaceHelper.INSTANCE.getLocalHostName());
    }
}
