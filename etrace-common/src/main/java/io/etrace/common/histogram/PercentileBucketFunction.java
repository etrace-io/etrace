package io.etrace.common.histogram;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class PercentileBucketFunction implements BucketFunction {
    private final int baseIndex;
    private final long baseNumber;
    private long maxNumber;
    private int maxSlot;

    private PercentileBucketFunction(long baseNumber) {
        int baseIndex = PercentileBuckets.indexOf(baseNumber);
        if (baseIndex > 0 && baseNumber == PercentileBuckets.get(baseIndex - 1)) {
            baseIndex--;
        }
        this.baseIndex = baseIndex;
        this.baseNumber = baseNumber;
        this.maxSlot = DistributionConstant.MAX_SLOT;
    }

    public static PercentileBucketFunction getFunctions(long baseNumber) {
        return new PercentileBucketFunction(baseNumber);
    }

    public List<String> getCategories() {
        return new AbstractList<String>() {
            @Override
            public String get(int index) {
                return getDistribution(index);
            }

            @Override
            public int size() {
                return maxSlot;
            }

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < maxSlot;
                    }

                    @Override
                    public String next() {
                        return getDistribution(index++);
                    }
                };
            }
        };
    }

    public long getAmount(int index) {
        if (index <= 0) {
            return PercentileBuckets.get(baseIndex);
        } else if (index >= maxSlot - 1) {
            return PercentileBuckets.get(baseIndex + maxSlot - 1);
        } else {
            return PercentileBuckets.get(baseIndex + index);
        }
    }

    public String getDistribution(int index) {
        if (index <= 0) {
            return String.format("[0,%s]", PercentileBuckets.get(baseIndex));
        } else if (index >= maxSlot - 1) {
            return String.format("(%s,∞+)", PercentileBuckets.get(baseIndex + maxSlot - 2));
        } else {
            return String.format("(%s,%s]", PercentileBuckets.get(baseIndex + index - 1),
                PercentileBuckets.get(baseIndex + index));
        }
    }

    public long getBaseNumber() {
        return baseNumber;
    }

    public int getMaxSlot() {
        return maxSlot;
    }

    @Override
    public DistributionType getDistributionType() {
        return DistributionType.Percentile;
    }

    @Override
    public int indexOf(long amount) {
        int amountIndex = PercentileBuckets.indexOf(amount);
        if (amountIndex > 0 && amount == PercentileBuckets.get(amountIndex - 1)) {
            amountIndex--;
        }
        int index = amountIndex - baseIndex;
        if (index < 0) {
            return 0;
        } else if (index >= maxSlot) {
            return maxSlot - 1;
        }
        return index;
    }

}
