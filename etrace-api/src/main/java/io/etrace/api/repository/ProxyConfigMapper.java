package io.etrace.api.repository;

import io.etrace.api.model.po.misc.ProxyConfig;
import org.springframework.data.repository.CrudRepository;

public interface ProxyConfigMapper extends CrudRepository<ProxyConfig, Long> {

    ProxyConfig findByProxyPath(String proxyPath);
}
