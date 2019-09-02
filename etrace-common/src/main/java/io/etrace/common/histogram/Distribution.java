package io.etrace.common.histogram;

public class Distribution {
    private BucketFunction bucketFunction;
    private DistributionType distributionType;
    private int maxSlot;
    private long baseNumber;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private long sum;
    private long count;
    private long[] values;

    public Distribution(BucketFunction bucketFunction) {
        this.distributionType = bucketFunction.getDistributionType();
        this.baseNumber = bucketFunction.getBaseNumber();
        this.maxSlot = bucketFunction.getMaxSlot();
        this.bucketFunction = bucketFunction;
        this.values = new long[maxSlot];
    }

    public Distribution(DistributionType distributionType, long baseNumber, int maxSlot) {
        this.distributionType = distributionType;
        this.baseNumber = baseNumber;
        this.maxSlot = maxSlot;
        this.bucketFunction = DistAlgorithmBucket.buildBucketFunction(distributionType, baseNumber);
        this.values = new long[maxSlot];
    }

    public void merge(Distribution other) {
        recordMin(other.min);
        recordMax(other.max);
        recordSum(other.sum);
        recordCount(other.count);
        long[] thatValues = other.values;
        if (thatValues != null) {
            for (int i = 0; i < thatValues.length; i++) {
                if (thatValues[i] > 0 && i < maxSlot) {
                    values[i] += thatValues[i];
                }
            }
        }
    }

    public void record(long amount) {
        record(bucketFunction.indexOf(amount), amount);
    }

    public void record(int index, long amount) {
        if (index < 0 || index >= maxSlot) {
            throw new RuntimeException(String.format("index: %s  is not between 0 and %s", index, values.length));
        }
        recordMin(amount);
        recordMax(amount);
        recordSum(amount);
        values[index]++;
        count++;
    }

    public void recordSum(long amount) {
        sum += amount;
    }

    public void recordMin(long amount) {
        if (min > amount) {
            min = amount;
        }
    }

    public void recordCount(long count) {
        this.count += count;
    }

    public void recordMax(long amount) {
        if (max < amount) {
            max = amount;
        }
    }

    public long[] getValues() {
        return values;
    }

    public void setValues(long[] values) {
        this.values = values;
    }

    public DistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public long getBaseNumber() {
        return baseNumber;
    }

    public void setBaseNumber(long baseNumber) {
        this.baseNumber = baseNumber;
    }

    public int getMaxSlot() {
        return maxSlot;
    }

    public void setMaxSlot(int maxSlot) {
        this.maxSlot = maxSlot;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public BucketFunction getBucketFunction() {
        return bucketFunction;
    }

    public void setBucketFunction(BucketFunction bucketFunction) {
        this.bucketFunction = bucketFunction;
    }

    public boolean isEmpty() {
        return (min > max) || values == null;
    }
}
