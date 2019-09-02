package io.etrace.common.histogram;

import io.etrace.common.exception.MetricAnalyzerException;

public class DistAlgorithmBucket {

    public static BucketFunction buildBucketFunction(DistributionType distributionType, long baseNumber) {
        switch (distributionType) {
            case Percentile:
                return PercentileBucketFunction.getFunctions(baseNumber);
            default:
                throw new MetricAnalyzerException("Unknown distribution type :" + distributionType);
        }
    }
}
