package io.etrace.common.histogram;

import org.junit.Test;

public class PercentileBucketFunctionTest {

    @Test
    public void testGetAmount() {
        BucketFunction bucketFunction = PercentileBucketFunction.getFunctions(0);
        System.out.println(bucketFunction.getCategories());
        assert 1 == bucketFunction.getAmount(0);
        assert 6 == bucketFunction.getAmount(5);
        assert 78643 == bucketFunction.getAmount(98);
        assert 91750 == bucketFunction.getAmount(99);
        assert 91750 == bucketFunction.getAmount(1000);
        bucketFunction = PercentileBucketFunction.getFunctions(30);
        System.out.println(bucketFunction.getCategories());
        System.out.println("0 : " + bucketFunction.getDistribution(0));
        assert 30 == bucketFunction.getAmount(0);
        System.out.println("10 : " + bucketFunction.getDistribution(10));
        assert 59 == bucketFunction.getAmount(10);
        System.out.println("31 : " + bucketFunction.getDistribution(31));
        assert 256 == bucketFunction.getAmount(31);
        System.out.println("98 : " + bucketFunction.getDistribution(98));
        assert 1677721 == bucketFunction.getAmount(98);
        System.out.println("99 : " + bucketFunction.getDistribution(99));
        assert 1887436 == bucketFunction.getAmount(99);
        System.out.println("100 : " + bucketFunction.getDistribution(100));
        assert 1887436 == bucketFunction.getAmount(100);
        System.out.printf(bucketFunction.getAmount(101) + "");
    }
}