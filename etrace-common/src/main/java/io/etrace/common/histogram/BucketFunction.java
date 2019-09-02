package io.etrace.common.histogram;

import java.util.List;

/**
 * @author yufu.deng Date: 17/3/2 Time: 17:01
 */
public interface BucketFunction {
    long getBaseNumber();

    int getMaxSlot();

    DistributionType getDistributionType();

    int indexOf(long amount);

    String getDistribution(int index);

    List<String> getCategories();

    long getAmount(int index);
}
