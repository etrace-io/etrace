package io.etrace.plugins.interceptors.tags;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

public class MetricsTagProviderAdapter implements MetricsTagProvider {
    @Override
    public Map<String, String> clientHttpRequestTags(HttpRequest request,
                                                     ClientHttpResponse response) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> httpRequestTags(HttpServletRequest request,
                                               HttpServletResponse response, Object handler) {
        return Collections.emptyMap();
    }
}
