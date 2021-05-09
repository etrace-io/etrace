package io.etrace.api.ratelimit;

import com.google.common.base.Strings;
import io.etrace.agent.Trace;
import io.etrace.api.model.RateLimitRequest;
import io.etrace.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Set;

public abstract class AbstractRateLimit implements BaseRateLimitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRateLimit.class);

    protected String type;
    protected RateLimitConfig rateLimitConfig;

    public AbstractRateLimit(String type) {
        this(type, null);
    }

    public AbstractRateLimit(String type, RateLimitConfig rateLimitConfig) {
        if (Strings.isNullOrEmpty(type)) {
            throw new IllegalArgumentException("type must not be null");
        }
        this.type = type;
        if (null == rateLimitConfig) {
            rateLimitConfig = new RateLimitConfig(200, 5);
        }
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean isAllowed(RateLimitRequest rateLimitrequest) {
        if (!rateLimitConfig.getEnable()) {
            Trace.logEvent("RateLimit", "NotStartRateLimit", Constants.SUCCESS);
            // 没有开启限流
            return true;
        }
        if (rateLimitrequest.getMaxToken() <= 0) {
            rateLimitrequest.setMaxToken(rateLimitConfig.getMaxToken());
        }
        if (rateLimitrequest.getRate() <= 0) {
            rateLimitrequest.setRate(5);
        }
        Set<String> blackSet = rateLimitConfig.getBlackSet();
        if (null != blackSet && blackSet.contains(rateLimitrequest.getKey())) {
            Trace.logEvent("RateLimit", "BlackSet", Constants.FAILURE);
            return false;
        }
        Set<String> whiteSet = rateLimitConfig.getWhiteSet();
        if (null != whiteSet && whiteSet.contains(rateLimitrequest.getKey())) {
            Trace.logEvent("RateLimit", "WhiteSet", Constants.SUCCESS);
            return true;
        }
        boolean allowed = true;
        try {
            allowed = isAllowed0(rateLimitrequest);
        } catch (Exception e) {
            LOGGER.error("RateLimit exception", e);
        }
        // 限流开启，但是不做限流操作，可以用作限流是否正确起作用限制
        if (!allowed && !rateLimitConfig.getDoRateLimit()) {
            Trace.logEvent("RateLimit", "DoNotRateLimit", Constants.SUCCESS);
            return true;
        }
        if (allowed) {
            Trace.logEvent("RateLimit", "Allow", Constants.SUCCESS);
        } else {
            Trace.logEvent("RateLimit", "NOtAllow", Constants.SUCCESS);
        }
        return allowed;
    }

    /**
     * 执行真正的限流逻辑，判断是否允许请求通过
     *
     * @param rateLimitrequest
     * @return
     */
    protected abstract boolean isAllowed0(RateLimitRequest rateLimitrequest);

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT1M")
    public void updateConfig() {
        RateLimitConfig rateLimitConfig = null;
        try {
            rateLimitConfig = pullRateLimitConfig();
        } catch (Exception e) {
            LOGGER.error("pull config error", e);
        }
        if (null != rateLimitConfig) {
            this.rateLimitConfig = rateLimitConfig;
            LOGGER.info("new Ratelimit config:{}", this.rateLimitConfig);
        }
    }

    /**
     * 更新配置，1分钟拉取一次
     */
    protected abstract RateLimitConfig pullRateLimitConfig() throws IOException;
}
