package io.etrace.collector.service;

import com.google.common.base.Strings;
import io.etrace.agent.config.DiskFileConfiguration;
import io.etrace.common.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ClusterService {

    @PostConstruct
    public void postConstruct() {
        // todo: 将此 DiskFileConfiguration  抽象出去
        DiskFileConfiguration configuration = new DiskFileConfiguration();
        configuration.loadParameter();

        String cluster = configuration.getCluster();
        if (Strings.isNullOrEmpty(cluster) || Constants.UNKNOWN.equals(cluster)) {
            // todo: move to non-open-source project
            throw new RuntimeException("cannot get cluster config from /etc/eleme/env.yaml.");
        }
        System.out.println("current node's cluster is :" + cluster);
        this.currentCluster = cluster;
    }

    private String currentCluster;

    public String getCurrentCluster() {
        return currentCluster;
    }
}
