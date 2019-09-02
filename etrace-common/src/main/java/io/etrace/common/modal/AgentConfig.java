package io.etrace.common.modal;

public class AgentConfig {
    private long id;
    private String configKey;
    private boolean enabled = true;
    private boolean aopEnabled = true;
    private int tagCount;
    private int tagSize;
    private int dataSize;
    private boolean longConnection = true;
    private int messageCount;
    private int redisSize;

    public AgentConfig() {
    }

    public AgentConfig(String configKey, boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize) {
        this(configKey, enabled, aopEnabled, tagCount, tagSize, dataSize, true);
    }

    public AgentConfig(String configKey, boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize,
                       boolean longConnection, int messageCount) {
        this.configKey = configKey;
        this.enabled = enabled;
        this.aopEnabled = aopEnabled;
        this.tagCount = tagCount;
        this.tagSize = tagSize;
        this.dataSize = dataSize;
        this.longConnection = longConnection;
        this.messageCount = messageCount;
    }

    public AgentConfig(String configKey, boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize,
                       boolean longConnection) {
        this.configKey = configKey;
        this.enabled = enabled;
        this.aopEnabled = aopEnabled;
        this.tagCount = tagCount;
        this.tagSize = tagSize;
        this.dataSize = dataSize;
        this.longConnection = longConnection;
    }

    public AgentConfig(boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize) {
        this(null, enabled, aopEnabled, tagCount, tagSize, dataSize, true);
    }

    public AgentConfig(boolean enabled, boolean aopEnabled, int tagCount, int tagSize, int dataSize,
                       boolean longConnection) {
        this(null, enabled, aopEnabled, tagCount, tagSize, dataSize, longConnection);
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTagCount() {
        return tagCount;
    }

    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
    }

    public int getTagSize() {
        return tagSize;
    }

    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public boolean isLongConnection() {
        return longConnection;
    }

    public void setLongConnection(boolean longConnection) {
        this.longConnection = longConnection;
    }

    public boolean isAopEnabled() {
        return aopEnabled;
    }

    public void setAopEnabled(boolean aopEnabled) {
        this.aopEnabled = aopEnabled;
    }

    public int getRedisSize() {
        return redisSize;
    }

    public void setRedisSize(int redisSize) {
        this.redisSize = redisSize;
    }
}
