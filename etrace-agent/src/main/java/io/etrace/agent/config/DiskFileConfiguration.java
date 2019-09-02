package io.etrace.agent.config;

import com.google.common.base.Strings;
import io.etrace.common.Constants;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class DiskFileConfiguration {

    private final static String ENV_PATH = "/etc/eleme/env.yaml"; //default env path
    private final static String APP_SPEC = "/appspec.yml";
    private final static String AONE_ENV_PATH = "/home/admin/env.yaml";
    private final static String AONE_APP_SPEC = "/home/admin/appspec.yml";

    private String cluster = Constants.UNKNOWN;
    private String ezone = Constants.UNKNOWN;
    private String idc = Constants.UNKNOWN;
    private String collectorIp = "";
    private int port = -1;

    public void loadParameter() {
        loadParameter0(ENV_PATH);
    }

    private void loadParameter0(String filePath) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> liveParameters = (Map<String, Object>)yaml.load(new FileInputStream(filePath));
            String cluster = getString(liveParameters, "cluster");
            String ezone = getString(liveParameters, "ezone");
            String idc = getString(liveParameters, "idc");
            String etrace_uri = getString(liveParameters, "etrace_uri");

            if (!Strings.isNullOrEmpty(cluster)) {
                this.cluster = cluster;
            }
            if (!Strings.isNullOrEmpty(ezone)) {
                this.ezone = ezone;
            }
            if (!Strings.isNullOrEmpty(idc)) {
                this.idc = idc;
            }
            if (!Strings.isNullOrEmpty(etrace_uri)) {
                buildCollectorIp(etrace_uri);
            }
        } catch (Exception ignore) {
        }
    }

    public void loadAppSpecParameter(String appId) {
        String appSpacPath = "/data/" + appId + APP_SPEC;
        loadAppSpecParameter0(appSpacPath);
    }

    private void loadAppSpecParameter0(String appSpacPath) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> appSpec = (Map<String, Object>)yaml.load(new FileInputStream(appSpacPath));
            String traceUri = null;
            if (appSpec != null) {
                Map<String, Object> runtimeParameters = (Map<String, Object>)appSpec.get("runtime");
                if (runtimeParameters != null) {
                    Object obj = runtimeParameters.get("trace_url");
                    if (obj != null) {
                        traceUri = obj.toString();
                    }
                }
            }
            if (!Strings.isNullOrEmpty(traceUri)) {
                buildCollectorIp(traceUri);
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * @since 3.0.0
     */
    @Deprecated
    // todo: delete
    public void loadAoneParameter() {
        loadParameter0(AONE_ENV_PATH);
        loadAppSpecParameter0(AONE_APP_SPEC);
    }

    public String getCluster() {
        return cluster;
    }

    public String getEzone() {
        return ezone;
    }

    public String getIdc() {
        return idc;
    }

    public String getCollectorIp() {
        return collectorIp;
    }

    public int getPort() {
        return port;
    }

    private String getString(Map<String, Object> liveParameters, String key) {
        String value = null;
        if (liveParameters != null) {
            Object obj = liveParameters.get(key);
            if (obj != null) {
                value = obj.toString();
            }
        }
        return value;
    }

    private void buildCollectorIp(String etraceUri) {
        if (Strings.isNullOrEmpty(etraceUri)) {
            return;
        }
        if (!etraceUri.startsWith("http://")) {
            etraceUri = "http://" + etraceUri;
        }
        try {
            URL url = new URL(etraceUri);
            this.collectorIp = url.getHost();
            this.port = url.getPort();
        } catch (MalformedURLException ignore) {
        }
    }
}
