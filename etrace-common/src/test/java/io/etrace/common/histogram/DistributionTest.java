package io.etrace.common.histogram;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class DistributionTest {

    // Number of positions of base-2 digits to shift when iterating over the long space.
    private static final int FIRST_DIGITS = 2;
    private static final int THEN_DIGITS = 1;
    private static final int[] change_exp;
    private static final int[] change_denominator;
    // Bucket values to use, see static block for initialization.
    private static long[] BUCKET_VALUES;
    // Keeps track of the positions for even powers of 4 within BUCKET_VALUES. This is used to
    // quickly compute the offset for a long without traversing the array.
    private static int[] POWER_OF_4_INDEX;

    static {
        int chage = 2;
        change_exp = new int[chage];
        change_exp[0] = 8;
        change_exp[1] = 10;
        change_denominator = new int[chage];
        change_denominator[0] = 5;
        change_denominator[1] = 7;

    }

    @Test
    public void testRecord() throws Exception {
        long baseNumber = 0;
        int pool = 1240297;
        PercentileBucketFunction bucketFunction = PercentileBucketFunction.getFunctions(baseNumber);
        //        percentileBucketFunction.adjust(10000000);
        int poolIndex = bucketFunction.indexOf(pool);
        System.out.println("distribution size : " + bucketFunction.getMaxSlot());
        System.out.println(bucketFunction.getCategories());
        Distribution distribution = new Distribution(bucketFunction);
        for (int i = 0; i < pool; i++) {
            distribution.record(i);
        }
        long[] data = distribution.getValues();
        int baseIndex = PercentileBuckets.indexOf(baseNumber);
        int count = pool;
        for (int i = 0; i < poolIndex; i++) {
            String dist = bucketFunction.getDistribution(i);
            String[] numbers = dist.substring(1, dist.length() - 1).split(",");
            double r = Long.valueOf(numbers[0]);
            if (r != 0) {
                r = data[i] / r;
            }
            System.out.println(
                String.format("assert i : %s  dist: %s  count: %s  percentage %s", i, dist, data[i], r * 100));
            if (i == 0) {
                assert data[i] == PercentileBuckets.get(baseIndex) + 1;
            } else {
                Assert.assertEquals(data[i], Long.valueOf(numbers[1]) - Long.valueOf(numbers[0]));
            }
            count -= data[i];
        }
        String dist = bucketFunction.getDistribution(poolIndex);
        System.out.println(String.format("assert i : %s  dist: %s  count: %s ", poolIndex, dist, data[poolIndex]));
        Assert.assertEquals(data[poolIndex], count);
    }

    @Test
    public void testPercentile() {
        ArrayList<Integer> powerOf4Index = new ArrayList<>();
        powerOf4Index.add(0);

        ArrayList<Long> buckets = new ArrayList<>();
        buckets.add(1L);
        buckets.add(2L);
        buckets.add(3L);

        int denominator = 3;
        int digits = FIRST_DIGITS;
        int exp = FIRST_DIGITS;
        while (exp < 64) {
            long current = 1L << exp;
            long delta = current / denominator;
            long next = (current << digits) - delta;

            powerOf4Index.add(buckets.size());
            while (current < next && current > 0) {
                buckets.add(current);
                current += delta;
            }
            exp += digits;
            if (exp == change_exp[0]) {
                digits = THEN_DIGITS;
                denominator = change_denominator[0];
            } else if (exp == change_exp[1]) {
                digits = THEN_DIGITS;
                denominator = change_denominator[1];
            }
        }
        buckets.add(Long.MAX_VALUE);

        BUCKET_VALUES = new long[buckets.size()];
        for (int i = 0; i < buckets.size(); ++i) {
            BUCKET_VALUES[i] = buckets.get(i);
        }

        POWER_OF_4_INDEX = new int[powerOf4Index.size()];
        for (int i = 0; i < powerOf4Index.size(); ++i) {
            POWER_OF_4_INDEX[i] = powerOf4Index.get(i);
        }
    }

    @Test
    public void testB() {
    }

    @Test
    public void testRatio() {
        double ratio = 0.05;
        ratio = 1 + ratio;
        long base = 1;
        long[] values = new long[100];
        for (int i = 0; i < values.length; i++) {
            if (i == 0) {
                values[i] = base;
            } else {
                long past = values[i - 1];
                long value = (long)(past * ratio);
                value = value > past ? value : past + 1;
                if (value >= 200) {
                    ratio = 1.1;
                }
                if (value >= 1000) {
                    ratio = 1.2;
                }
                values[i] = value;
            }
        }

        for (int i = 0; i < values.length; i++) {
            System.out.println(values[i]);
        }
    }
}