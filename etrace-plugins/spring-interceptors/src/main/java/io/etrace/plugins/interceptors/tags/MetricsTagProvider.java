package io.etrace.plugins.interceptors.tags;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface MetricsTagProvider {
    /**
     * @param request  RestTemplate client HTTP request
     * @param response may be null in the event of a client error
     * @return a map of tags added to every client HTTP request metric
     */
    Map<String, String> clientHttpRequestTags(HttpRequest request,
                                              ClientHttpResponse response);

    /**
     * @param request  HTTP request
     * @param response HTTP response
     * @param handler  the request method that is responsible for handling the request
     * @return a map of tags added to every Spring MVC HTTP request metric
     */
    Map<String, String> httpRequestTags(HttpServletRequest request,
                                        HttpServletResponse response, Object handler);

}
