package io.etrace.api.ratelimit;

import io.etrace.api.model.RateLimitRequest;

public interface BaseRateLimitService {

    boolean isAllowed(RateLimitRequest rateLimitrequest);

}
