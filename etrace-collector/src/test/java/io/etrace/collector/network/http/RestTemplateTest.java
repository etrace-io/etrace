package io.etrace.collector.network.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Ignore("无法正常工作")
public class RestTemplateTest {

    private RestTemplate restTemplate;

    @Before
    public void before() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(2);
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(5000)
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(3000)
            .build();
        HttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager)
            .build();
        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        restTemplate = new RestTemplate(factory);
    }

    @Test
    public void httpTest() {
        String url = "http://127.0.0.1:2890/throughput";
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        //System.out.println(response.getBody());
    }

    @After
    public void after() {
    }
}
