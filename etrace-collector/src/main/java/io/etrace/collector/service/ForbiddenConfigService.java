package io.etrace.collector.service;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import io.etrace.common.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 用于拒绝打点
 */
@Singleton
public class ForbiddenConfigService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ForbiddenConfigService.class);
    private final static String FORBIDDEN_APPIDS = "forbidden_appids";
    private final static String FORBIDDEN_METRIC = "forbidden_metrics";
    private volatile Set<String> forbiddenAppids = new HashSet<>();
    private volatile Map<String, List<ForbiddenMetricsPolicy>> policiesByAppid = new HashMap<>();

    private static class ForbiddenMetricsConfig {
        private String key;
        private String appId;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }
    }

    private static class ForbiddenMetricsPolicy {

        private static enum PolicyType {
            START_WITH_MATCH, END_WITH_MATCH, FULL_MATCH, NONE;
        }

        private PolicyType type = PolicyType.FULL_MATCH;

        private final String pattern;

        private final String appId;

        public ForbiddenMetricsPolicy(String appId, ForbiddenMetricsConfig config) {
            this.appId = appId;
            String key = config.getKey();
            if (Strings.isNullOrEmpty(config.getKey())) {
                type = PolicyType.NONE;
                pattern = "";
            } else if (key.startsWith("*")) {
                type = PolicyType.END_WITH_MATCH;
                pattern = key.substring(1).trim();
            } else if (key.endsWith("*")) {
                type = PolicyType.START_WITH_MATCH;
                pattern = key.substring(0, key.length() - 1).trim();
            } else {
                pattern = key.trim();
                type = PolicyType.FULL_MATCH;
            }
        }

        public boolean isForbiddenMetricName(String metricName) {
            if (Strings.isNullOrEmpty(metricName)) {
                return false;
            }
            switch (type) {
            case FULL_MATCH:
                return metricName.equals(pattern);
            case START_WITH_MATCH:
                return metricName.startsWith(pattern);
            case END_WITH_MATCH:
                return metricName.endsWith(pattern);
            case NONE:
                return false;
            default:
                return false;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("ForbiddenMetricsPolicy{");
            sb.append("type=").append(type);
            sb.append(", pattern='").append(pattern).append('\'');
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ForbiddenMetricsPolicy that = (ForbiddenMetricsPolicy) o;

            if (type != that.type)
                return false;
            if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null)
                return false;
            if (appId != null ? !appId.equals(that.appId) : that.appId != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
            result = 31 * result + (appId != null ? appId.hashCode() : 0);
            return result;
        }
    }

    public void trySetForbiddenAppidConfig(Map<String, String> newConfig) {
        trySetSystemConfig(newConfig, FORBIDDEN_APPIDS, () -> forbiddenAppids, value -> forbiddenAppids = Sets.newHashSet(Splitter.on(",").split(value)),
                () -> forbiddenAppids = new HashSet<>());
    }

    public void trySetForbiddenMetricConfig(Map<String, String> newConfig) {
        trySetSystemConfig(newConfig, FORBIDDEN_METRIC, () -> policiesByAppid, value -> {
            try {
                List<ForbiddenMetricsConfig> configs = JSONUtil.toArray(value, ForbiddenMetricsConfig.class);
                policiesByAppid =
                    configs.stream().filter(c -> !Strings.isNullOrEmpty(c.appId) && !Strings.isNullOrEmpty(c.key))
                        .map(c -> new ForbiddenMetricsPolicy(c.getAppId(), c)).collect(Collectors.groupingBy(c -> c.appId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return policiesByAppid;
        }, () -> policiesByAppid = new HashMap<>());
    }

    /**
     *
     * @param newConfig
     *      传入的配置项Map
     * @param key
     *      配置项key
     * @param oldValueCallBack
     *      callback,返回内存里该配置项之前的结构(如List,Set,Map等)
     * @param newValueCallBack
     *      callback,传入配置项的新的value字符串,返回解析后新的结构
     * @param emptyConfigOrExeptionInitCallBack
     *      callback,当出现传入配置项的新的value字符串为空,或者处理期间出现异常时的回调,并返回内存里该配置项更新后的结构
     */
    public static void trySetSystemConfig(Map<String, String> newConfig, // config map
            String key, // config key 
            Supplier<Object> oldValueCallBack,//return old value
            Function<String, Object> newValueCallBack, // input config-string-value,output newvalue 
            Supplier<Object> emptyConfigOrExeptionInitCallBack) {
        String valueStr = null;
        try {
            valueStr = newConfig.get(key);
            Object old = oldValueCallBack.get();
            Object newValue = Strings.isNullOrEmpty(valueStr) ? emptyConfigOrExeptionInitCallBack.get() :
                newValueCallBack.apply(valueStr);
            if (!Objects.equals(old, newValue)) {
                LOGGER.info("{} updated: {} -> {}", key, old, newValue);
            }
        } catch (Exception e) {
            LOGGER.error("{}:{} failover value -> {}", key, valueStr, emptyConfigOrExeptionInitCallBack.get(), e);
        }
    }

    public boolean isForbiddenAppId(String appId) {
        return !forbiddenAppids.isEmpty() && forbiddenAppids.contains(appId);
    }

    public boolean isForbiddenMetricName(String appId, String metricName) {
        Map<String, List<ForbiddenMetricsPolicy>> policyMap = policiesByAppid;
        if (Strings.isNullOrEmpty(metricName) || policyMap.isEmpty() || Strings.isNullOrEmpty(appId)) {
            return false;
        }
        List<ForbiddenMetricsPolicy> policyList = policyMap.get(appId);
        if (Objects.isNull(policyList) || policyList.isEmpty()) {
            return false;
        }
        for (ForbiddenMetricsPolicy policy : policyList) {
            if (policy.isForbiddenMetricName(metricName)) {
                return true;
            }
        }
        return false;
    }
}
