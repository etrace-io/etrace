package io.etrace.api.service.proxy;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
public class ProxyRequest {

    private HttpMethod httpMethod;

    private Map<String, String> params;

    private String body;

    private String proxyPath;
}
