package io.etrace.common.modal.metric;

public class MetricConfig {
    private boolean enabled = true;
    private int tagCount;
    private int tagSize;
    private int maxPackageCount;
    private int maxMetric;
    private int maxGroup;
    private int maxHistogramGroup;
    private int aggregatorTime;

    public MetricConfig() {
        this(true, 8, 256, 1000, 100, 10000, 1000, 1000);
    }

    public MetricConfig(boolean enabled, int tagCount, int tagSize, int maxPackageCount, int maxMetric, int maxGroup,
                        int maxHistogramGroup, int aggregatorTime) {
        this.enabled = enabled;
        this.tagCount = tagCount;
        this.tagSize = tagSize;
        this.maxPackageCount = maxPackageCount;
        this.maxMetric = maxMetric;
        this.maxGroup = maxGroup;
        this.maxHistogramGroup = maxHistogramGroup;
        this.aggregatorTime = aggregatorTime;
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

    public int getMaxPackageCount() {
        return maxPackageCount;
    }

    public void setMaxPackageCount(int maxPackageCount) {
        this.maxPackageCount = maxPackageCount;
    }

    public int getMaxMetric() {
        return maxMetric;
    }

    public void setMaxMetric(int maxMetric) {
        this.maxMetric = maxMetric;
    }

    public int getMaxGroup() {
        return maxGroup;
    }

    public void setMaxGroup(int maxGroup) {
        this.maxGroup = maxGroup;
    }

    public int getMaxHistogramGroup() {
        return maxHistogramGroup;
    }

    public void setMaxHistogramGroup(int maxHistogramGroup) {
        this.maxHistogramGroup = maxHistogramGroup;
    }

    public int getAggregatorTime() {
        return aggregatorTime;
    }

    public void setAggregatorTime(int aggregatorTime) {
        this.aggregatorTime = aggregatorTime;
    }
}
