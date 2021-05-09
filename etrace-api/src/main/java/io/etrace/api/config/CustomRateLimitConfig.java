package io.etrace.api.config;

import io.etrace.api.ratelimit.AsyncRateLimit;
import io.etrace.api.ratelimit.BaseRateLimitService;
import io.etrace.api.ratelimit.RateLimitConfig;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CustomRateLimitConfig {

    public static final String OPEN_API_RATE_LIMIT = "openApiRateLimit";

    @Bean(name = OPEN_API_RATE_LIMIT, autowire = Autowire.BY_TYPE)
    public BaseRateLimitService buidOpenApiConfig() {
        RateLimitConfig rateLimitConfig = new RateLimitConfig(true, false, 200, 5);
        AsyncRateLimit asyncRateLimit = new AsyncRateLimit("OepnApi", rateLimitConfig);
        return asyncRateLimit;
    }
}
