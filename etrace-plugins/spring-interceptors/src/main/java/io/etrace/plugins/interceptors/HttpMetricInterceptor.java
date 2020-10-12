package io.etrace.plugins.interceptors;

import com.google.common.collect.Lists;
import io.etrace.plugins.interceptors.tags.MetricsTagProvider;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpMetricInterceptor extends HandlerInterceptorAdapter {
    private boolean useTimer;
    private Tags commonTags;
    private Collection<MetricsTagProvider> tagProviders;

    private ThreadLocal<Long> contexts = new ThreadLocal<>();

    public HttpMetricInterceptor(boolean useTimer, Tags commonTags, Collection<MetricsTagProvider> tagProviders) {
        this.useTimer = useTimer;
        this.commonTags = commonTags;
        this.tagProviders = tagProviders;
    }

    public HttpMetricInterceptor() {
        this(true, Tags.empty(), Lists.newArrayList());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (useTimer) {
            contexts.set(System.nanoTime());
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        try {
            String uri = request.getRequestURI();
            Tags tags = Tags.of(commonTags).and("uri", uri);
            if (null == ex) {
                tags.and("type", "success");
            } else {
                tags.and("type", "fail");
            }
            for (MetricsTagProvider tagProvider : tagProviders) {
                Map<String, String> httpTags = tagProvider.httpRequestTags(request, response,
                    handler);
                for (Map.Entry<String, String> tag : httpTags.entrySet()) {
                    tags.and(tag.getKey(), tag.getValue());
                }
            }

            if (useTimer) {
                long duration = contexts.get() - System.nanoTime();
                Metrics.timer("http", tags).record(duration, TimeUnit.NANOSECONDS);
            } else {
                Metrics.counter("http", tags).increment();
            }
        } finally {
            contexts.remove();
        }

        super.afterCompletion(request, response, handler, ex);
    }

}
