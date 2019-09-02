package io.etrace.common.modal;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class SpanItem {
    private long start;
    private long end;
    private String hostIp;
    private String hostName;
    private String type;
    private String name;
    private String tags;
    private Map<String, Integer> ips = new HashMap<>();
    private String status;
    private String messageId;
    private long id;
    private int messageCount;
    private String blockKey;
    private String sampleId;

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Map<String, Integer> getIps() {
        return ips;
    }

    public void addIp(String ip, int messageCount) {
        this.messageCount += messageCount;
        ips.put(ip, messageCount);
    }

    public String getBlockKey() {
        return blockKey;
    }

    public void setBlockKey(String blockKey) {
        this.blockKey = blockKey;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
