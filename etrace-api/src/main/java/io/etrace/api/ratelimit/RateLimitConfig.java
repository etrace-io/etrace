package io.etrace.api.ratelimit;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public class RateLimitConfig {

    /**
     * 是否开启限流
     */
    private Boolean enable = Boolean.TRUE;
    /**
     * 当被限流时候是否真正执行限流操作
     */
    private Boolean doRateLimit = Boolean.TRUE;
    /**
     * 每秒最大qps,作为兜底设置，
     */
    private Integer maxToken;
    /**
     * 1秒内分段的数目，不要太多也不能太少，设置4-10，建议设置为能被1000整除的值 作为兜底设置，
     */
    private Integer rate;
    /**
     * 黑名单，在黑名单之内的直接拒绝
     */
    private Set<String> blackSet;

    /**
     * 白名单，不做限流，直接允许
     */
    private Set<String> whiteSet;

    public RateLimitConfig(Integer maxToken, Integer rate) {
        this(null, null, maxToken, rate);
    }

    public RateLimitConfig(Boolean enable, Boolean doRateLimit, Integer maxToken, Integer rate) {
        this(enable, doRateLimit, maxToken, rate, null, null);

    }

    public RateLimitConfig(Integer maxToken, Integer rate, Set<String> blackSet, Set<String> whiteSet) {
        this(null, null, maxToken, rate, blackSet, whiteSet);
    }

    public RateLimitConfig(Boolean enable, Boolean doRateLimit, Integer maxToken, Integer rate, Set<String> blackSet,
                           Set<String> whiteSet) {
        this.enable = enable == null ? Boolean.TRUE : enable;
        this.doRateLimit = doRateLimit == null ? Boolean.TRUE : doRateLimit;
        if (maxToken == null || maxToken <= 0) {
            throw new IllegalArgumentException("max token must be large than zerr");
        }
        this.maxToken = maxToken;
        if (rate == null || rate <= 0) {
            throw new IllegalArgumentException("rate must be large than zerr");
        }
        this.rate = rate;
        this.blackSet = blackSet == null ? Sets.newHashSet() : blackSet;
        this.whiteSet = whiteSet == null ? Sets.newHashSet() : whiteSet;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable == null ? Boolean.TRUE : enable;
    }

    public void setDoRateLimit(Boolean doRateLimit) {
        this.doRateLimit = doRateLimit == null ? Boolean.TRUE : doRateLimit;
    }

    public void setMaxToken(Integer maxToken) {
        if (maxToken == null || maxToken <= 0) {
            throw new IllegalArgumentException("max token must be large than zerr");
        }
        this.maxToken = maxToken;
    }

    public void setRate(Integer rate) {
        if (rate == null || rate <= 0) {
            throw new IllegalArgumentException("rate must be large than zerr");
        }
        this.rate = rate;
    }

    public void setBlackSet(Set<String> blackSet) {
        this.blackSet = blackSet == null ? Sets.newHashSet() : blackSet;
    }

    public void setWhiteSet(Set<String> whiteSet) {
        this.whiteSet = whiteSet == null ? Sets.newHashSet() : whiteSet;
    }
}
