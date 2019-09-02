package io.etrace.common.modal.metric;

/**
 * Record the cost time of service
 * <p>
 * The timer doesn't support the quantile(upper_85,upper_90 etc.), but another metric type : Histogram do.
 * timer.setUpperEnable(false), means you get the metric type : Timer. Otherwise, it's Histogram.(default)
 * <p>
 * Timer has the following fields: avg, sum, count, min, max (avg = sum / count) Besides those fields, Histogram has
 * some more: upper_{\d+} : quantile(upper_85,upper_90 etc.);
 */
public interface Timer extends Metric<Timer> {

    /**
     * set this.value = (cost time), count = 1 As soon as value(long value) invoked, it means that this metric ends with
     * the value. The latter operations are all invalid.
     * <p>
     * For example: timer.value(100); // effective, the instance finishes. timer.end(); // invalid timer.value(100); //
     * invalid timer.addTag("key-2", "value-2"); // invalid timer.value(1);  //invalid
     *
     * @param value the time cost
     */
    void value(long value);

    /**
     * As soon as this method invoked, it means that this metric ends with the value. The latter operations are all
     * invalid.
     * <p>
     * For example: timer.end(); // effective, the instance finishes. timer.end(); // invalid timer.value(100); //
     * invalid timer.addTag("key-2", "value-2"); // invalid timer.value(1);  //invalid
     */
    void end();

    boolean isUpperEnable();

    /**
     * default: true, set the quantile enable
     * <p>
     * The default timer calculates the quantile(upper_85,upper_90 etc.), that means the metric type is: Histogram. When
     * do setUpperEnable(false), like : timer.setUpperEnable(false), then the metric type is: Timer .
     *
     * @param upperEnable the quantile enable
     * @return
     */
    Timer setUpperEnable(boolean upperEnable);
}
