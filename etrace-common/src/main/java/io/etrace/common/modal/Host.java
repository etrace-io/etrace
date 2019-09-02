package io.etrace.common.modal;

import java.io.Serializable;

public class Host implements Serializable {
    private static final long serialVersionUID = -4718016666929311935L;
    private Integer id;
    private Integer appId;
    private String hostIp;
    private String hostName;

    public Host() {
    }

    public Host(Integer appId, String hostIp, String hostName) {
        this.appId = appId;
        this.hostIp = hostIp;
        this.hostName = hostName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String toString() {
        return "Host{" +
            "id=" + id +
            ", appId=" + appId +
            ", hostIp='" + hostIp + '\'' +
            ", hostName='" + hostName + '\'' +
            '}';
    }
}
