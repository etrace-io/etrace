package io.etrace.common.histogram;

public class DistPoint {
    private BucketFunction bucketFunction;
    private long amount;

    public DistPoint(BucketFunction bucketFunction, long amount) {
        this.bucketFunction = bucketFunction;
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public BucketFunction getBucketFunction() {
        return bucketFunction;
    }

    public void setBucketFunction(BucketFunction bucketFunction) {
        this.bucketFunction = bucketFunction;
    }
}
