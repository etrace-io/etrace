package io.etrace.api.service;

import io.etrace.api.consts.ApiConstants;
import io.etrace.api.model.ProxyResponse;
import io.etrace.api.model.po.misc.Config;
import io.etrace.api.model.po.misc.ProxyConfig;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

// todo: maybe can use https://github.com/mitre/HTTP-Proxy-Servlet to proxy implement
// or just use RestTemplate
@Service
public class ProxyService {

    public static final Long HTTP_TIME_OUT = 10000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyService.class);
    @Autowired
    private ProxyConfigService proxyConfigService;

    @Autowired
    private ConfigHolder configHolder;

    @Autowired
    private RestTemplate restTemplate;

    public void mergeResponse(Map<String, ResponseEntity<Object>> idcResponse, ProxyResponse result) {
        idcResponse.forEach((idc, httpResponse) -> {
            if (null != httpResponse) {
                if (HttpStatus.OK == httpResponse.getStatusCode()) {
                    result.addResult(HttpStatus.OK, httpResponse.getBody());
                } else {
                    result.addResult(httpResponse.getStatusCode(), httpResponse.getBody());
                }
            } else {
                result.addResult(HttpStatus.INTERNAL_SERVER_ERROR, "http response return null");
            }
        });
    }

    public ProxyResponse getResult00(HttpServletRequest request, HttpMethod method, String body, String proxyPrefix)
        throws InterruptedException {
        ProxyResponse result = new ProxyResponse();
        String originUri = request.getRequestURI().substring(proxyPrefix.length());
        String realProxyPath = originUri.substring(0,
            originUri.contains("/") ? originUri.indexOf("/") : originUri.length() - 1);

        ProxyConfig config = proxyConfigService.getConfigByUniqueKey(realProxyPath);
        if (null != config) {
            String path = config.getPath();
            //step1: get real path
            String postPath = originUri.substring(realProxyPath.length());
            //step2: get request server
            Map<String/*idc*/, String> urls = getProxyServers(config.getServerName(), config.getClusters());
            //step3: request
            if (null != urls && urls.size() > 0) {
                // if only one ezone ,do not use async
                if (urls.size() == 1) {
                    for (Map.Entry<String/*idc*/, String> entry : urls.entrySet()) {
                        String ip = entry.getValue();
                        if (StringUtils.isNotEmpty(ip)) {
                            String proxyUrl = ip + postPath + "?" + request.getQueryString();
                            ResponseEntity<Object> resp = restTemplate.exchange(proxyUrl, method,
                                new HttpEntity<>(body), Object.class);
                            result.addResult(resp.getStatusCode(), resp.getBody());
                        }
                    }
                } else {
                    List<Future<ResponseEntity<Object>>> futureList = new ArrayList<>(urls.size());
                    CountDownLatch latch = new CountDownLatch(urls.size());
                    List<String> idcList = new ArrayList<>(urls.size());
                    for (Map.Entry<String/*idc*/, String> entry : urls.entrySet()) {
                        String idc = entry.getKey();
                        String ip = entry.getValue();
                        idcList.add(idc);
                        Future<ResponseEntity<Object>> future = new FutureTask<>(() -> {
                            String proxyUrl = ip + postPath + "?" + request.getQueryString();
                            ResponseEntity<Object> resp = restTemplate.exchange(proxyUrl, method,
                                new HttpEntity<>(body), Object.class);
                            return resp;
                        });
                        futureList.add(future);
                    }
                    boolean await = latch.await(HTTP_TIME_OUT, TimeUnit.MILLISECONDS);
                    if (!await) {
                        result.addResult(HttpStatus.INTERNAL_SERVER_ERROR, "waiting too long!");
                        return result;
                    } else {
                        Map<String, ResponseEntity<Object>> idcResponse = new HashMap<>(urls.size());
                        for (int i = 0; i < futureList.size(); i++) {
                            try {
                                idcResponse.put(idcList.get(i), futureList.get(i).get());
                            } catch (Exception ignore) {
                                //
                                LOGGER.error("async future get error", ignore);
                            }
                        }
                        mergeResponse(idcResponse, result);
                    }
                }
            } else {
                result.addResult(HttpStatus.INTERNAL_SERVER_ERROR, "not found proxy server urls.");
            }
        } else {
            result.addResult(HttpStatus.INTERNAL_SERVER_ERROR, "not found proxy path.");
        }
        return result;
    }

    private Map<String, String> getProxyServers(String serverKey, String clusters) {
        //console
        if (ApiConstants.ETRACE_CONSOLE_URL.equals(serverKey)) {
            return proxyConfigService.getConsoleUrl(clusters);
        }
        String[] clusterArr = clusters.split(",");
        Map<String, String> urls = newHashMap();
        List<Config> configs = newArrayList();
        for (String cluster : clusterArr) {
            configs.addAll(configHolder.queryConfigByCache(cluster, null, serverKey));
        }
        if (!CollectionUtils.isEmpty(configs)) {
            configs.forEach(config -> urls.put(config.getIdc(), config.getValue()));
        }
        return urls;

    }
}
