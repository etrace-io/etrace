package io.etrace.stream.aggregator;

/**
 * 聚合函数的个数与group by result的个数近似
 */
public class FlushCounter {
    private static ThreadLocal<FlushCounter> threadLocal = ThreadLocal.withInitial(FlushCounter::new);

    private long counter;

    private FlushCounter() {
    }

    public static void reset() {
        threadLocal.get().counter = 0;
    }

    public static long get() {
        return threadLocal.get().counter;
    }

    public static long increase(int value) {
        return threadLocal.get().counter += value;
    }

    public static long increase() {
        return increase(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        FlushCounter that = (FlushCounter)o;

        return counter == that.counter;
    }

    @Override
    public int hashCode() {
        return (int)(counter ^ (counter >>> 32));
    }
}
