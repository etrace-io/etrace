package io.etrace.common.modal.metric;

/**
 * Counter has following fields: count to record the counter.
 * <p>
 * As soon as once() or value(long count) invoked, it means that this metric ends with the value.The latter operations
 * are all invalid.
 * <p>
 * For example: counter.once();  // effective, and the instance finishes. counter.addTag("key-2", "value-2"); // invalid
 * counter.once();  // invalid counter.value(10);  // invalid
 * <p>
 * Besides, value(long count) is the same as the once()
 */
public interface Counter extends Metric<Counter> {

    /**
     * set value = 1
     */
    void once();

    /**
     * set value = count
     *
     * @param count set count
     */
    void value(long count);
}
