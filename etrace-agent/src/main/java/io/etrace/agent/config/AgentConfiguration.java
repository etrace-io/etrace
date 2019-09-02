package io.etrace.agent.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.etrace.common.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AgentConfiguration {

    private static volatile String appId = System.getProperty("APPID");
    private static volatile String collectorIp = "";
    private static volatile int port = 2890;
    private static volatile String serviceName;
    private static volatile String team = System.getProperty("TEAM");
    private static volatile String instance = null;
    private static volatile String mesosTaskId = System.getenv("MESOS_TASK_ID");
    private static volatile String eleapposLabel = System.getenv("ELEAPPOS_LABEL");
    private static volatile String eleapposSlaveFqdn = System.getenv("ELEAPPOS_SLAVE_FQDN");

    private static DiskFileConfiguration diskFileConfiguration;

    private static volatile Set<String> beanObjectNames = new HashSet<>();

    private static Map<String, String> globalTags = null;

    /**
     * 是否开启trace，融合完成后不再向collector发送trace流量，只保留metric
     */
    private static volatile boolean enableTrace = true;

    public static Map<String, String> getGlobalTags() {
        return globalTags;
    }

    /**
     * User can use this api to set some global tags, like: host=xxx;ezone=bbb; then, all Trace and metrics will have
     * those tags. should be called only once!
     *
     * @param tags
     */
    public static void setGlobalTags(Map<String, String> tags) {
        globalTags = ImmutableMap.copyOf(tags);
    }

    public static String getMesosTaskId() {
        return mesosTaskId;
    }

    public static void setMesosTaskId(String mesosTaskId) {
        AgentConfiguration.mesosTaskId = mesosTaskId;
    }

    public static String getEleapposLabel() {
        return eleapposLabel;
    }

    public static void setEleapposLabel(String eleapposLabel) {
        AgentConfiguration.eleapposLabel = eleapposLabel;
    }

    public static String getEleapposSlaveFqdn() {
        return eleapposSlaveFqdn;
    }

    public static void setEleapposSlaveFqdn(String eleapposSlaveFqdn) {
        AgentConfiguration.eleapposSlaveFqdn = eleapposSlaveFqdn;
    }

    public static String getAppId() {
        return !Strings.isNullOrEmpty(appId) ? appId : Constants.UNKNOWN_APP_ID;
    }

    public static void setAppId(String appId) {
        AgentConfiguration.appId = appId;
    }

    public static String getCollectorIp() {
        if (diskFileConfiguration != null && !Strings.isNullOrEmpty(diskFileConfiguration.getCollectorIp())) {
            return diskFileConfiguration.getCollectorIp();
        }
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
        if (diskFileConfiguration != null && diskFileConfiguration.getPort() != -1) {
            return diskFileConfiguration.getPort();
        }
        return port;
    }

    public static String getServiceName() {
        if (!Strings.isNullOrEmpty(serviceName)) {
            return serviceName;
        } else if (!Strings.isNullOrEmpty(appId)) {
            return appId;
        } else {
            return Constants.UNKNOWN_APP_ID;
        }
    }

    public static void setServiceName(String serviceName) {
        AgentConfiguration.serviceName = serviceName;
    }

    public static String getTeam() {
        return team;
    }

    public static void setTeam(String team) {
        AgentConfiguration.team = team;
    }

    public static String getInstance() {
        if (instance != null && instance.length() > 50) {
            instance = instance.substring(0, 50);
        }
        return instance;
    }

    public static void setInstance(String instance) {
        AgentConfiguration.instance = instance;
    }

    public static DiskFileConfiguration getDiskFileConfiguration() {
        DiskFileConfiguration diskFileConfiguration = AgentConfiguration.diskFileConfiguration;
        if (diskFileConfiguration == null) {
            diskFileConfiguration = new DiskFileConfiguration();
            diskFileConfiguration.loadAoneParameter();
            diskFileConfiguration.loadParameter();
            diskFileConfiguration.loadAppSpecParameter(appId);
            AgentConfiguration.diskFileConfiguration = diskFileConfiguration;
        }
        return diskFileConfiguration;
    }

    public static void addMBean(String objectName) {
        beanObjectNames.add(objectName);
    }

    public static void removeMBean(String objectName) {
        beanObjectNames.remove(objectName);
    }

    public static Set<String> getBeanObjectNames() {
        return beanObjectNames;
    }

    public static boolean isEnableTrace() {
        return enableTrace;
    }

    public static void setEnableTrace(boolean enableTrace) {
        AgentConfiguration.enableTrace = enableTrace;
    }
}
