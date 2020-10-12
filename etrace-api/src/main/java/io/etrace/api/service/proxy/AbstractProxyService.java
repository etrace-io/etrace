package io.etrace.api.service.proxy;

import com.google.common.base.Strings;
import org.springframework.util.CollectionUtils;

import java.util.List;

public abstract class AbstractProxyService {

    /**
     * @param request
     * @return
     */
    public String proxy(ProxyRequest request) throws Exception {
        String realPath = getPath(request.getProxyPath());
        if (Strings.isNullOrEmpty(realPath)) {
            throw new Exception(request.getProxyPath() + " could not find real path");
        }
        List<String> proxyServers = getProxyServers();
        if (CollectionUtils.isEmpty(proxyServers)) {
            throw new Exception("current proxy  could not find the determine proxy server list");
        }
        return null;
    }

    /**
     * 根据代理的path获取真实path
     *
     * @param proxyPath
     * @return
     */
    protected String getPath(String proxyPath) {
        return null;
    }

    protected List<String> getProxyServers() {
        return null;
    }

    protected abstract Object handlerProxyResult(String res);
}
