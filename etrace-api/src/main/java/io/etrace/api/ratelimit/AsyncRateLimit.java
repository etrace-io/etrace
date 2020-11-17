package io.etrace.api.ratelimit;

import com.google.common.base.Strings;
import io.etrace.api.model.RateLimitRequest;
import io.etrace.api.model.po.misc.Config;
import io.etrace.api.service.ConfigService;
import io.etrace.common.util.JSONUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class AsyncRateLimit extends AbstractRateLimit {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRateLimit.class);
    private final Bucket bucket;
    @Autowired
    private ConfigService configService;

    /**
     * @param type
     * @param rateLimitConfig
     */
    public AsyncRateLimit(String type, RateLimitConfig rateLimitConfig) {
        super(type, rateLimitConfig);

        Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    protected boolean isAllowed0(RateLimitRequest rateLimitrequest) {
        return bucket.tryConsume(1);
    }

    @Override
    protected RateLimitConfig pullRateLimitConfig() throws IOException {
        List<Config> configList = configService.findByKey(type);
        if (CollectionUtils.isEmpty(configList)) {
            LOGGER.error("could not find config from db:{}", type);
            return null;
        }
        if (configList.size() != 1) {
            LOGGER.error("the config size is not zero:{}", configList);
            return null;
        }

        String config = configList.get(0).getValue();
        if (Strings.isNullOrEmpty(config)) {
            LOGGER.error("the config value is empty:{}", configList);
            return null;
        }
        LOGGER.info("pull {} ratelimit config:{}", type, config);
        RateLimitConfig rateLimitConfig = JSONUtil.toObject(config, RateLimitConfig.class);
        RateLimitConfig newConfig = new RateLimitConfig(rateLimitConfig.getEnable(), rateLimitConfig.getDoRateLimit(),
            rateLimitConfig.getMaxToken(), rateLimitConfig.getRate(), rateLimitConfig.getBlackSet(),
            rateLimitConfig.getWhiteSet());
        LOGGER.info("{} new config:{}", type, newConfig.toString());
        return newConfig;
    }
}
