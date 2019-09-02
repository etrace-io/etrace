package io.etrace.common.modal;

public class CallStack {
    private String requestId;
    private String id;
    private Message message;
    private String appId;
    private String hostIp;
    private String hostName;
    private String instance;
    private String cluster;
    private String ezone;
    private String idc;
    private String mesosTaskId;
    private String eleapposLabel;
    private String eleapposSlaveFqdn;

    public void clear() {
        message = null;
        requestId = null;
        id = null;
        appId = null;
        hostIp = null;
        hostName = null;
        instance = null;
        cluster = null;
        ezone = null;
        idc = null;
        mesosTaskId = null;
        eleapposLabel = null;
        eleapposSlaveFqdn = null;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
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

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getEzone() {
        return ezone;
    }

    public void setEzone(String ezone) {
        this.ezone = ezone;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public String getMesosTaskId() {
        return mesosTaskId;
    }

    public void setMesosTaskId(String mesosTaskId) {
        this.mesosTaskId = mesosTaskId;
    }

    public String getEleapposLabel() {
        return eleapposLabel;
    }

    public void setEleapposLabel(String eleapposLabel) {
        this.eleapposLabel = eleapposLabel;
    }

    public String getEleapposSlaveFqdn() {
        return eleapposSlaveFqdn;
    }

    public void setEleapposSlaveFqdn(String eleapposSlaveFqdn) {
        this.eleapposSlaveFqdn = eleapposSlaveFqdn;
    }
}
