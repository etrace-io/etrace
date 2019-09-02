package io.etrace.common.modal.metric;

/**
 * Gauge has the field: value to record the last value in the time point
 */
public interface Gauge extends Metric<Gauge> {
    /**
     * As soon as value(double count) invoked, it means that this metric ends with the value. The latter operations are
     * all invalid. For example: gauge.value(200); // effective, the instance finishes. gauge.value(100); // invalid
     * gauge.addTag("key-2", "value-2"); // invalid gauge.value(1);  //invalid
     *
     * @param value the last value
     */
    void value(double value);
}
