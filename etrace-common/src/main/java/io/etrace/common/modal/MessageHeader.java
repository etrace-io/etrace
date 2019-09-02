package io.etrace.common.modal;

public class MessageHeader {
    private String appId;
    private String hostIp;
    private String hostName;
    private String instance;
    private String dalGroup;
    private String key;
    private long ast;//agent send time
    private long crt;//collector receive time
    private long cst;//collector send time
    private long csrt;//consumer receive time
    private String messageType;

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

    public long getAst() {
        return ast;
    }

    public void setAst(long ast) {
        this.ast = ast;
    }

    public long getCrt() {
        return crt;
    }

    public void setCrt(long crt) {
        this.crt = crt;
    }

    public long getCst() {
        return cst;
    }

    public void setCst(long cst) {
        this.cst = cst;
    }

    public long getCsrt() {
        return csrt;
    }

    public void setCsrt(long csrt) {
        this.csrt = csrt;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getDalGroup() {
        return dalGroup;
    }

    public void setDalGroup(String dalGroup) {
        this.dalGroup = dalGroup;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
