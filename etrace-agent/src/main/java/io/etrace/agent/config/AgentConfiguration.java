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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.etrace.common.constant.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * must configure: 1. appid 2. collector domain/ip (without port) 3. collector port 4. tenant optional configure: 1.
 * instance 2. globalTags 3. beanObjectNames extra configurations: 1. extraProperties: Map Also can implement your own
 * ConfigurationLoader to load configurations in your way: initByConfigurationLoader(loader) by default, this value can
 * be retrieved from System Properties.
 */
public class AgentConfiguration {
    public static final String APPID_SYSTEM_PROPERTY_KEY = "APPID";
    public static final String COLLECTOR_IP_SYSTEM_PROPERTY_KEY = "COLLECTOR_IP";
    public static final String TENANT_SYSTEM_PROPERTY_KEY = "TENANT";

    private static volatile String appId = System.getProperty(APPID_SYSTEM_PROPERTY_KEY);
    private static volatile String collectorIp = System.getProperty(COLLECTOR_IP_SYSTEM_PROPERTY_KEY);
    private static volatile String tenant = System.getProperty(TENANT_SYSTEM_PROPERTY_KEY);

    private static volatile int port = 2890;
    private static volatile String instance;

    private static volatile Set<String> beanObjectNames = new HashSet<>();
    private static Map<String, String> globalTags = Maps.newHashMap();

    private static Map<String, String> extraProperties;

    private static boolean debugMode = false;

    public static void initByConfigurationLoader(ConfigurationLoader loader) {
        setAppId(loader.getAppId());
        setCollectorIp(loader.getCollectorDomainAndPort());
        setTenant(loader.getTenant());
        setInstance(loader.getInstance());
        setGlobalTags(loader.getGlobalTags());
        setExtraProperties(loader.getExtraProperties());
    }

    public static Map<String, String> getGlobalTags() {
        return globalTags;
    }

    /**
     * User can use this api to set some global tags, like: host=xxx;ezone=bbb; then, all Trace and metrics will have
     * those tags. should be called only once!
     *
     * @param tags tags
     */
    public static void setGlobalTags(Map<String, String> tags) {
        if (tags != null) {
            globalTags = ImmutableMap.copyOf(tags);
        }
    }

    public static String getAppId() {
        return appId;
    }

    public static void setAppId(String appId) {
        AgentConfiguration.appId = !Strings.isNullOrEmpty(appId) ? appId : Constants.UNKNOWN_APP_ID;
    }

    public static String getTenant() {
        return AgentConfiguration.tenant;
    }

    public static void setTenant(String tenant) {
        AgentConfiguration.tenant = tenant;
    }

    public static String getCollectorIp() {
        return collectorIp;
    }

    public static void setCollectorIp(String collectorIp) {
        if (Strings.isNullOrEmpty(collectorIp)) {
            return;
        }
        if (!collectorIp.startsWith("http://")) {
            collectorIp = "http://" + collectorIp;
        }
        try {
            URL url = new URL(collectorIp);
            AgentConfiguration.collectorIp = url.getHost();
            int port = url.getPort();
            if (port == -1) {
                return;
            }
            AgentConfiguration.port = url.getPort();
        } catch (MalformedURLException e) {
            AgentConfiguration.collectorIp = collectorIp;
        }
    }

    public static int getCollectorPort() {
        return port;
    }

    public static String getInstance() {
        return instance;
    }

    public static void setInstance(String instance) {
        if (instance != null && instance.length() > 50) {
            instance = instance.substring(0, 50);
        }
        AgentConfiguration.instance = instance;
    }

    public static Set<String> getBeanObjectNames() {
        return beanObjectNames;
    }

    public static Map<String, String> getExtraProperties() {
        return extraProperties;
    }

    public static void setExtraProperties(Map<String, String> extraProperties) {
        AgentConfiguration.extraProperties = extraProperties;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        AgentConfiguration.debugMode = debugMode;
    }
}
