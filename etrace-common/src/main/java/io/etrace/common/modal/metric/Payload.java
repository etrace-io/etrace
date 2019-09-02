package io.etrace.common.modal.metric;

/**
 * Record the size and count of package
 * <p>
 * Payload has the following field: avg, sum, count, min, max (avg = sum / count)
 */
public interface Payload extends Metric<Payload> {
    /**
     * As soon as value(double count) invoked, it means that this metric ends with the value. The latter operations are
     * all invalid. For example: payload.value(200); // effective, the instance finishes. payload.value(100); // invalid
     * payload.addTag("key-2", "value-2"); // invalid payload.value(1);  //invalid
     *
     * @param value package size
     */
    void value(long value);
}
